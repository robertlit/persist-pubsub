resource "random_id" "gcf_source_bucket_name" {
  byte_length = 16
}

resource "google_storage_bucket" "gfc_source" {
  name                        = "${random_id.gcf_source_bucket_name.hex}-gcf-source"
  location                    = var.region
  uniform_bucket_level_access = true
}

data "archive_file" "code" {
  type        = "zip"
  output_path = "/tmp/gcf-source.zip"
  source_dir  = "../"
  excludes    = ["infrastructure", "build", ".gradle", ".idea", "README.md"]
}

resource "google_storage_bucket_object" "code_archive" {
  bucket = google_storage_bucket.gfc_source.name
  name   = "gcf-source.zip"
  source = data.archive_file.code.output_path
}

resource "google_cloudfunctions2_function" "publisher" {
  name        = "publisher-function"
  location    = var.region
  description = "a cloud function that publishes a message to a pub/sub topic"

  build_config {
    runtime     = "java17"
    entry_point = "me.robertlit.serverless.PublishFunction"

    source {
      storage_source {
        bucket = google_storage_bucket.gfc_source.name
        object = google_storage_bucket_object.code_archive.name
      }
    }
  }

  service_config {
    available_memory   = "512M"
    timeout_seconds    = 60

    environment_variables = {
      PROJECT      = var.project-id
      PUBSUB_TOPIC = var.pubsub_topic
    }
  }
}

resource "google_cloud_run_service_iam_binding" "public" {
  project  = google_cloudfunctions2_function.publisher.project
  location = google_cloudfunctions2_function.publisher.location
  service  = google_cloudfunctions2_function.publisher.name
  members  = ["allUsers"]
  role     = "roles/run.invoker"
}

output "publisher_function_uri" {
  value = google_cloudfunctions2_function.publisher.service_config[0].uri
}

resource "random_id" "pubsub_logs_bucket_name" {
  byte_length = 16
}

resource "google_storage_bucket" "pubsub_logs" {
  name                        = "${random_id.pubsub_logs_bucket_name.hex}-pubsub-logs"
  location                    = var.region
  uniform_bucket_level_access = true
}

resource "google_cloudfunctions2_function" "receiver" {
  name        = "receiver-function"
  location    = var.region
  description = "a cloud function that receives messages from pub/sub and logs them in cloud storage"

  build_config {
    runtime     = "java17"
    entry_point = "me.robertlit.serverless.ReceiveFunction"

    source {
      storage_source {
        bucket = google_storage_bucket.gfc_source.name
        object = google_storage_bucket_object.code_archive.name
      }
    }
  }

  service_config {
    available_memory   = "512M"
    timeout_seconds    = 60

    environment_variables = {
      BUCKET_NAME = google_storage_bucket.pubsub_logs.name
    }
  }

  event_trigger {
    trigger_region = var.region
    event_type     = "google.cloud.pubsub.topic.v1.messagePublished"
    pubsub_topic   = google_pubsub_topic.main.id
    retry_policy   = "RETRY_POLICY_RETRY"
  }
}

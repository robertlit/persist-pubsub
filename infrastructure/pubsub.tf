variable "pubsub_topic" {
  description = "pubsub topic to listen and publish to"
}

resource "google_pubsub_topic" "main" {
  name = var.pubsub_topic
}
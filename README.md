This project contains two Cloud Functions and the required infrastructure:
1. An HTTP Cloud Function that publishes the body of an HTTP request to a given Pub/Sub topic
2. A Cloud Events Function that receives Pub/Sub messages from a given topic and persists them to Cloud Storage

# Deploying

## Create a Google Cloud project
Create a new project in the GCP Console. Enable Cloud Build API,
Cloud Functions API, Cloud Logging API, Cloud Pub/Sub API, Eventarc API,
Artifact Registry API and Cloud Run Admin API.

## Set up the Google Cloud client
```
gcloud init
gcloud auth application-default login
```

## Run Terraform
In the `infrastructure` directory, configure the variables 
in `terraform.tfvars`, then run
```
terraform init
terraform apply
```
This will prepare the infrastructure, package and deploy the code

# Sources
- [Terraform HTTP Example](https://cloud.google.com/functions/docs/tutorials/terraform)
- [HTTP Tutorial (2nd gen)](https://cloud.google.com/functions/docs/tutorials/http)
- [Java Deployment Options](https://cloud.google.com/functions/docs/concepts/java-deploy)
- [Authenticating Functions (2nd gen) Calls](https://stackoverflow.com/a/75711889)
- [Event-Driven Functions](https://cloud.google.com/functions/docs/writing/write-event-driven-functions)
- [Event-Driven Function Retries](https://cloud.google.com/functions/docs/bestpractices/retries)
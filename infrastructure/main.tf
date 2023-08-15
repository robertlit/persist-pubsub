variable "project-id" {
  description = "project id"
}

variable "region" {
  description = "region"
}

provider "google" {
  project = var.project-id
  region  = var.region
}
terraform {
  required_version = ">= 1.10"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }

  # 段階1では state をローカル（infra/terraform.tfstate）で管理する。
}

provider "aws" {
  region  = var.aws_region
  profile = var.aws_profile

  # 全リソースに付けるタグ。請求の確認や消し忘れの検索に使う。
  default_tags {
    tags = {
      Project   = var.project_name
      ManagedBy = "terraform"
    }
  }
}

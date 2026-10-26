variable "aws_region" {
  description = "AWS リージョン"
  type        = string
  default     = "ap-northeast-1"
}

variable "aws_profile" {
  description = "AWS CLI のプロファイル名（aws configure --profile で作成したもの）"
  type        = string
  default     = "taskmanagement"
}

variable "project_name" {
  description = "リソース名やタグに使うプロジェクト名"
  type        = string
  default     = "taskmanagement"
}

variable "vpc_cidr" {
  description = "VPC の IP アドレス範囲"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "パブリックサブネットの IP アドレス範囲（AZ ごとに 1 つ）"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "プライベートサブネット（RDS 用）の IP アドレス範囲。RDS は AZ の異なる 2 つ以上が必要"
  type        = list(string)
  default     = ["10.0.11.0/24", "10.0.12.0/24"]
}

variable "instance_type" {
  description = "EC2 のインスタンスタイプ"
  type        = string
  default     = "t3.micro"
}

variable "my_ip" {
  description = "80 と 22 番ポートへの接続を許可する自分の IP（例: 203.0.113.10/32）"
  type        = string

  validation {
    condition     = can(cidrhost(var.my_ip, 0)) && endswith(var.my_ip, "/32")
    error_message = "my_ip は「IP アドレス/32」の形式で指定してください（例: 203.0.113.10/32）。"
  }
}

variable "ssh_public_key_path" {
  description = "EC2 に登録する SSH 公開鍵のパス"
  type        = string
  default     = "~/.ssh/id_ed25519.pub"
}

variable "db_instance_class" {
  description = "RDS のインスタンスクラス"
  type        = string
  default     = "db.t4g.micro"
}

variable "db_name" {
  description = "RDS に作るデータベース名（ローカルの docker-compose と同じ）"
  type        = string
  default     = "taskmanagement"
}

variable "db_username" {
  description = "RDS のマスターユーザー名"
  type        = string
  default     = "taskmanagement"
}

variable "db_password" {
  description = "RDS のマスターユーザーのパスワード（terraform.tfvars で指定する）"
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.db_password) >= 8 && !can(regex("[/@\" ]", var.db_password))
    error_message = "db_password は 8 文字以上で、/ @ \" と空白を含めないでください。"
  }
}

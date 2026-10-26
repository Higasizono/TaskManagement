output "instance_id" {
  description = "EC2 のインスタンス ID（起動・停止に使う）"
  value       = aws_instance.app.id
}

output "public_ip" {
  description = "EC2 のパブリック IP（起動のたびに変わる）"
  value       = aws_instance.app.public_ip
}

output "app_url" {
  description = "アプリの URL"
  value       = "http://${aws_instance.app.public_ip}"
}

output "ssh_command" {
  description = "EC2 に SSH でログインするコマンド"
  value       = "ssh ec2-user@${aws_instance.app.public_ip}"
}

output "rds_endpoint" {
  description = "RDS の接続先ホスト名（VPC の中からのみ接続できる）"
  value       = aws_db_instance.main.address
}

output "rds_port" {
  description = "RDS のポート番号"
  value       = aws_db_instance.main.port
}

output "psql_command" {
  description = "EC2 上で RDS に接続するコマンド（パスワードは terraform.tfvars の db_password）"
  value       = "psql -h ${aws_db_instance.main.address} -U ${var.db_username} -d ${var.db_name}"
}

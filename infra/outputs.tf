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

# アプリには認証がないため、インターネット全体ではなく自分の IP からの接続だけを許可する。
resource "aws_security_group" "ec2" {
  name        = "${var.project_name}-ec2-sg"
  description = "Allow app (8080) and SSH (22) from my IP only"
  vpc_id      = aws_vpc.main.id

  tags = {
    Name = "${var.project_name}-ec2-sg"
  }
}

# Spring Boot が API と React の画面をまとめて 8080 番で配信する。
resource "aws_vpc_security_group_ingress_rule" "app" {
  security_group_id = aws_security_group.ec2.id
  description       = "App (Spring Boot: API + React)"
  ip_protocol       = "tcp"
  from_port         = 8080
  to_port           = 8080
  cidr_ipv4         = var.my_ip
}

resource "aws_vpc_security_group_ingress_rule" "ssh" {
  security_group_id = aws_security_group.ec2.id
  description       = "SSH"
  ip_protocol       = "tcp"
  from_port         = 22
  to_port           = 22
  cidr_ipv4         = var.my_ip
}

# パッケージのインストールなど、EC2 からの外向き通信はすべて許可する。
resource "aws_vpc_security_group_egress_rule" "all" {
  security_group_id = aws_security_group.ec2.id
  description       = "All outbound"
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
}

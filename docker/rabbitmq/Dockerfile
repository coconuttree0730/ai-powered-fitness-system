FROM rabbitmq:3.13-management

# 将本地插件复制到正确的容器目录
COPY ./plugins/rabbitmq_delayed_message_exchange-3.13.0.ez /opt/rabbitmq/plugins/

# 启用插件
RUN rabbitmq-plugins enable --offline rabbitmq_delayed_message_exchange

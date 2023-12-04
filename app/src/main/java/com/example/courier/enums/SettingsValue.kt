package com.example.courier.enums

enum class SettingsValue(val value: String) {

    TOKEN("token"),
    PROTOCOL("protocol"),
    SERVER_NAME("server_name"),
    SERVER_PORT("server_port"),
    BACK_QUEUE_NAME("back_queue_name"),
    RABBIT_USERNAME("rabbit_username"),
    RABBIT_PASSWORD("rabbit_password"),
    RABBIT_SERVER_NAME("rabbit_server_name"),
    RABBIT_SERVER_PORT("rabbit_server_port")
}
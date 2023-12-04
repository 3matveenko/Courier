package com.example.courier.enums

enum class RabbitCode(val value: String) {
    LOGOUT("logout"),
    LOCATION("location"),
    SEND_SMS("send_sms"),
    NEW_ORDER("new_order"),
    REJECT_ORDER("reject_order"),
    ACCEPT_ORDER("accept_order"),
    ORDER_SUCCESS("order_success"),
    SEND_SMS_STATUS("send_sms_success"),
    NEW_ORDER_REJECTED("new_order_rejected"),
    ACCEPT_REJECT_ORDER("accept_rejected_order"),
    ORDER_SUCCESS_NOT_SOLD("order_success_not_sold"),
    GET_MY_ORDERS_STATUS_PROGRESSING("get_my_orders_status_progressing")
}
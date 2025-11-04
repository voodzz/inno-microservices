package com.innowise.paymentservice.model;

/**
 * Defines the possible states of a payment transaction.
 */
public enum StatusEnum {
    PENDING,
    SUCCESS,
    FAILED,
    CANCELED,
    REFUNDED
}
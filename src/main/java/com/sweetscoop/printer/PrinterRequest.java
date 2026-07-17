package com.sweetscoop.printer;

//예시: 필드명이 'content'가 아니라 'receipt'인 경우
public class PrinterRequest {
 private String receipt; // 필드명 변경
 public PrinterRequest(String receipt) { this.receipt = receipt; }
 public String getReceipt() { return receipt; }
}
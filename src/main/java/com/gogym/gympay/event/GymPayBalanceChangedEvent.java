package com.gogym.gympay.event;

import com.gogym.gympay.entity.GymPay;
import com.gogym.gympay.entity.constant.TransferType;

public record GymPayBalanceChangedEvent(TransferType transferType,
                                        int amount,
                                        int balance,
                                        Long counterpartyId,
                                        GymPay gymPay) {
}

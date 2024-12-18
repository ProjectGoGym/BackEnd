package com.gogym.gympay.event;

import com.gogym.chat.entity.ChatRoom;
import com.gogym.member.entity.Member;

public record StartTransactionEvent(ChatRoom chatRoom,
                                    Member seller,
                                    Member buyer) {

}

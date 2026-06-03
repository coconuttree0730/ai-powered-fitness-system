package com.fitness.modules.chat.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatStreamEventVO {

    private String type;

    private String text;

    private String message;

    public static ChatStreamEventVO delta(String text) {
        return new ChatStreamEventVO("delta", text, null);
    }

    public static ChatStreamEventVO status(String message) {
        return new ChatStreamEventVO("status", null, message);
    }

    public static ChatStreamEventVO done() {
        return new ChatStreamEventVO("done", null, null);
    }

    public static ChatStreamEventVO error(String message) {
        return new ChatStreamEventVO("error", null, message);
    }

    /**
     * HITL：需要用户确认的操作
     * @param pendingActionsJson JSON 格式的待确认操作列表
     */
    public static ChatStreamEventVO approvalRequired(String pendingActionsJson) {
        return new ChatStreamEventVO("approval_required", pendingActionsJson, null);
    }
}

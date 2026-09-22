package com.studyhub.entity;

/**
 * 笔记状态常量
 */
public class NoteStatus {

    /** 待审核 */
    public static final Integer PENDING = 0;

    /** 已通过（公开） */
    public static final Integer APPROVED = 1;

    /** 已拒绝 */
    public static final Integer REJECTED = 2;

    /** 已删除 */
    public static final Integer DELETED = 3;

    private NoteStatus() {
    }
}

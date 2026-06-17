package com.studyhub.dto;

/**
 * 仪表盘统计数据响应
 */
public class DashboardResponse {

    private Long totalUsers;
    private Long totalNotes;
    private Long todayNotes;
    private Long totalComments;
    private Long pendingNotes;
    private Long totalViews;

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getTotalNotes() {
        return totalNotes;
    }

    public void setTotalNotes(Long totalNotes) {
        this.totalNotes = totalNotes;
    }

    public Long getTodayNotes() {
        return todayNotes;
    }

    public void setTodayNotes(Long todayNotes) {
        this.todayNotes = todayNotes;
    }

    public Long getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(Long totalComments) {
        this.totalComments = totalComments;
    }

    public Long getPendingNotes() {
        return pendingNotes;
    }

    public void setPendingNotes(Long pendingNotes) {
        this.pendingNotes = pendingNotes;
    }

    public Long getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(Long totalViews) {
        this.totalViews = totalViews;
    }
}

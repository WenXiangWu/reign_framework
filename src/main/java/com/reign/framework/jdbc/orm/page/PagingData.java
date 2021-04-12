package com.reign.framework.jdbc.orm.page;

/**
 * @ClassName: PagingData
 * @Description: 分页处理类
 * @Author: wuwx
 * @Date: 2021-04-08 18:16
 **/
public class PagingData {

    //总页数
    private int pagesCount;

    //总行数
    private int rowsCount;

    //当前页
    private int currentPage;

    //每页显示行数
    private int rowsPerPage;


    public int getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(int pagesCount) {
        this.pagesCount = pagesCount;
    }

    public int getRowsCount() {
        return rowsCount;
    }

    public void setRowsCount(int rowsCount) {
        this.rowsCount = rowsCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }


    /**
     * 设置更新总页数
     */
    public void setPagesCount() {
        //fail safe
        if (this.rowsPerPage == 0) {
            this.pagesCount = 0;
            return;
        }
        //计算总页数
        this.pagesCount = (int) Math.ceil(this.rowsCount * 1.0 / this.rowsPerPage);
        while (this.currentPage >= this.pagesCount) {
            this.currentPage = this.currentPage - 1;
        }
        this.currentPage = this.currentPage < 0 ? 0 : this.currentPage;

    }


    /**
     * 首页
     */
    public void pageTop() {
        if (pagesCount > 1 && rowsPerPage > 0) {
            currentPage = 1;
        }
    }


    /**
     * 前一页
     */
    public void pagePrevious() {
        if (pagesCount > 1 && rowsPerPage > 0) {
            if (currentPage > 1) {
                currentPage--;
            }

        }
    }

    /**
     * 下一页
     */
    public void pageNext() {
        if (pagesCount > 1 && rowsPerPage > 0) {
            if (currentPage < pagesCount) {
                currentPage++;
            }

        }
    }

    /**
     * 末页
     */
    public void pageLast() {
        if (pagesCount > 1 && rowsPerPage > 0) {
            if (currentPage < pagesCount) {
                currentPage = pagesCount;
            }

        }
    }

}

package net.momirealms.customfishing.data;

public class PlayerSellData {

    private double money;
    private int date;

    public PlayerSellData(double money, int date) {
        this.money = money;
        this.date = date;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

}

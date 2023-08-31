package net.momirealms.customfishing.api.common;

public class Tuple<L, M, R> {

    private L left;
    private M mid;
    private R right;

    public Tuple(L left, M mid, R right) {
        this.left = left;
        this.mid = mid;
        this.right = right;
    }

    public static <L, M, R> Tuple<L, M, R> of(final L left, final M mid, final R right) {
        return new Tuple<>(left, mid, right);
    }

    public L getLeft() {
        return left;
    }

    public void setLeft(L left) {
        this.left = left;
    }

    public M getMid() {
        return mid;
    }

    public void setMid(M mid) {
        this.mid = mid;
    }

    public R getRight() {
        return right;
    }

    public void setRight(R right) {
        this.right = right;
    }
}
package net.momirealms.customfishing.util;

public class MatrixUtils {

    public static <T> T[][] rotate90(T[][] matrix) {
        int rows = matrix.length;
        if (rows == 0) {
            return matrix;
        }
        int cols = matrix[0].length;

        @SuppressWarnings("unchecked")
        T[][] rotated = (T[][]) new Object[cols][rows];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                rotated[c][rows - 1 - r] = matrix[r][c];
            }
        }
        return rotated;
    }

    public static <T> void mirrorHorizontally(T[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        for (int i = 0; i < rows / 2; i++) {
            for (int j = 0; j < cols; j++) {
                T temp = matrix[i][j];
                matrix[i][j] = matrix[rows - i - 1][j];
                matrix[rows - i - 1][j] = temp;
            }
        }
    }

    public static <T> void mirrorVertically(T[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols / 2; j++) {
                T temp = matrix[i][j];
                matrix[i][j] = matrix[i][cols - j - 1];
                matrix[i][cols - j - 1] = temp;
            }
        }
    }
}

/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.api.storage.data;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

/**
 * The EarningData class holds data related to the earnings of a player from selling fish.
 * It includes the total earnings and the date of the earnings record.
 */
public class EarningData {

    @SerializedName("earnings")
    public double earnings;
    @SerializedName("date")
    public int date;

    /**
     * Constructs a new EarningData instance with specified earnings and date.
     *
     * @param earnings the total earnings from fishing.
     * @param date the date of the earnings record.
     */
    public EarningData(double earnings, int date) {
        this.earnings = earnings;
        this.date = date;
        this.refresh();
    }

    /**
     * Creates an instance of EarningData with default values (zero earnings and date).
     *
     * @return a new instance of EarningData with default values.
     */
    public static EarningData empty() {
        return new EarningData(0d, 0);
    }

    public EarningData copy() {
        return new EarningData(earnings, date);
    }

    public void refresh() {
        Calendar calendar = Calendar.getInstance();
        int dat = (calendar.get(Calendar.MONTH) +1) * 100 + calendar.get(Calendar.DATE);
        if (dat != date) {
            date = dat;
            earnings = 0;
        }
    }
}

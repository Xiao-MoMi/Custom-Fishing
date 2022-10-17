///*
// *  Copyright (C) <2022> <XiaoMoMi>
// *
// *  This program is free software: you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation, either version 3 of the License, or
// *  any later version.
// *
// *  This program is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU General Public License for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
// */
//
//package net.momirealms.customfishing.util;
//
//import net.momirealms.customfishing.object.totem.CorePos;
//import net.momirealms.customfishing.object.totem.FinalModel;
//import net.momirealms.customfishing.object.totem.OriginalModel;
//import org.bukkit.Location;
//
//public class TotemUtil {
//
//    public static int checkLocationModel(OriginalModel model, Location location){
//
//        CorePos corePos = model.getCorePos();
//        int xOffset = corePos.getX();
//        int yOffset = corePos.getY();
//        int zOffset = corePos.getZ();
//
//        int height = model.getHeight();
//        int length = model.getLength();
//        int width = model.getWidth();
//
//        //从第一层开始逐层扫描，只有一层满足要求才能扫描上一层，否则跳入下一个方向检测
//        Location startLoc = location.clone().subtract(0, yOffset, 0);
//
//        Label_1:
//        {
//            for(int i = 0; i< height; i++) {
//                //起点定于左下角，向右上遍历
//                Location loc = startLoc.clone().add(-xOffset, i, -zOffset);
//                for (int z = 0; z < width; z++) {
//                    inner: for (int x = 0; x < length; x++) {
//                        String[] elements = model.getElement(x, z, i);
//                        String id = BlockUtil.getId(loc.clone().add(x, 0, z));
//                        for (String element : elements) {
//                            if (element.equals("*")) continue inner;
//                            if (id == null) break;
//                            if (id.equals(element)) continue inner;
//                        }
//                        break Label_1;
//                    }
//                }
//            }
//            return 1;
//        }
//
//        Label_2:
//        {
//            for (int i = 0; i < height; i++) {
//                //起点定于右上角，向左下遍历
//                Location loc = startLoc.clone().add(xOffset, i, zOffset);
//                for (int z = 0; z < width; z++) {
//                    inner: for (int x = 0; x < length; x++) {
//                        String[] elements = model.getElement(x, z, i);
//                        String id = BlockUtil.getId(loc.clone().add(-x, 0, -z));
//                        for (String element : elements) {
//                            if (element.equals("*")) continue inner;
//                            if (id == null) break;
//                            if (id.equals(element)) continue inner;
//                        }
//                        break Label_2;
//                    }
//                }
//            }
//            return 2;
//        }
//
//        Label_3:
//        {
//            for (int i = 0; i < height; i++) {
//                //起点定于左上角，向右下遍历
//                Location loc = startLoc.clone().add(-zOffset, i, xOffset);
//                for (int z = 0; z < width; z++) {
//                    inner: for (int x = 0; x < length; x++) {
//                        String[] elements = model.getElement(x, z, i);
//                        String id = BlockUtil.getId(loc.clone().add(z, 0, -x));
//                        for (String element : elements) {
//                            if (element.equals("*")) continue inner;
//                            if (id == null) break;
//                            if (id.equals(element)) continue inner;
//                        }
//                        break Label_3;
//                    }
//                }
//            }
//            return 3;
//        }
//
//        Label_4:
//        {
//            for (int i = 0; i < height; i++) {
//                //起点定于右下角，向左上遍历
//                Location loc = startLoc.clone().add(zOffset, i, -xOffset);
//                for (int z = 0; z < width; z++) {
//                    inner: for (int x = 0; x < length; x++) {
//                        String[] elements = model.getElement(x, z, i);
//                        String id = BlockUtil.getId(loc.clone().add(-z, 0, x));
//                        for (String element : elements) {
//                            if (element.equals("*")) continue inner;
//                            if (id == null) break;
//                            if (id.equals(element)) continue inner;
//                        }
//                        break Label_4;
//                    }
//                }
//            }
//            return 4;
//        }
//
//        Label_5:
//        {
//            for (int i = 0; i < height; i++) {
//                //起点定于左下角（镜像），向上左遍历
//                Location loc = startLoc.clone().add(-zOffset, i, -xOffset);
//                for (int z = 0; z < width; z++) {
//                    inner: for (int x = 0; x < length; x++) {
//                        String[] elements = model.getElement(x, z, i);
//                        String id = BlockUtil.getId(loc.clone().add(z, 0, x));
//                        for (String element : elements) {
//                            if (element.equals("*")) continue inner;
//                            if (id == null) break;
//                            if (id.equals(element)) continue inner;
//                        }
//                        break Label_5;
//                    }
//                }
//            }
//            return 5;
//        }
//
//        Label_6:
//        {
//            for (int i = 0; i < height; i++) {
//                //起点定于右上角（镜像），向下左遍历
//                Location loc = startLoc.clone().add(zOffset, i, xOffset);
//                for (int z = 0; z < width; z++) {
//                    inner: for (int x = 0; x < length; x++) {
//                        String[] elements = model.getElement(x, z, i);
//                        String id = BlockUtil.getId(loc.clone().add(-z, 0, -x));
//                        for (String element : elements) {
//                            if (element.equals("*")) continue inner;
//                            if (id == null) break;
//                            if (id.equals(element)) continue inner;
//                        }
//                        break Label_6;
//                    }
//                }
//            }
//            return 6;
//        }
//
//        Label_7:
//        {
//            for (int i = 0; i < height; i++) {
//                //起点定于左上角（镜像)，向右下遍历
//                Location loc = startLoc.clone().add(-xOffset, i, zOffset);
//                for (int z = 0; z < width; z++) {
//                    inner: for (int x = 0; x < length; x++) {
//                        String[] elements = model.getElement(x, z, i);
//                        String id = BlockUtil.getId(loc.clone().add(x, 0, -z));
//                        for (String element : elements) {
//                            if (element.equals("*")) continue inner;
//                            if (id == null) break;
//                            if (id.equals(element)) continue inner;
//                        }
//                        break Label_7;
//                    }
//                }
//            }
//            return 7;
//        }
//        Label_8:
//        {
//            for (int i = 0; i < height; i++) {
//                //起点定于右下角（镜像），向左上遍历
//                Location loc = startLoc.clone().add(xOffset, i, -zOffset);
//                for (int z = 0; z < width; z++) {
//                    inner: for (int x = 0; x < length; x++) {
//                        String[] elements = model.getElement(x, z, i);
//                        String id = BlockUtil.getId(loc.clone().add(-x, 0, z));
//                        for (String element : elements) {
//                            if (element.equals("*")) continue inner;
//                            if (id == null) break;
//                            if (id.equals(element)) continue inner;
//                        }
//                        break Label_8;
//                    }
//                }
//            }
//            return 8;
//        }
//        return 0;
//    }
//
//
//    public static void removeModel(FinalModel model, Location location, int id) {
//
//        CorePos corePos = model.getCorePos();
//        int xOffset = corePos.getX();
//        int yOffset = corePos.getY();
//        int zOffset = corePos.getZ();
//
//        int height = model.getHeight();
//        int length = model.getLength();
//        int width = model.getWidth();
//
//        //从第一层开始逐层扫描，只有一层满足要求才能扫描上一层，否则跳入下一个方向检测
//        Location startLoc = location.clone().subtract(0, yOffset, 0);
//
//        switch (id) {
//            case 1:
//                for (int i = 0; i < height; i++) {
//                    //起点定于左下角，向右上遍历
//                    Location loc = startLoc.clone().add(-xOffset, i, -zOffset);
//                    for (int z = 0; z < width; z++)
//                        for (int x = 0; x < length; x++) {
//                            if (model.getElement(x, z, i) == null) {
//                                BlockUtil.remove(loc.clone().add(x, 0, z));
//                            }
//                            else if (!model.getElement(x, z, i).equals("*")){
//                                BlockUtil.replace(loc.clone().add(x, 0, z), model.getElement(x, z, i));
//                            }
//                        }
//                }
//                break;
//            case 2:
//                for (int i = 0; i < height; i++) {
//                    //起点定于右上角，向左下遍历
//                    Location loc = startLoc.clone().add(xOffset, i, zOffset);
//                    for (int z = 0; z < width; z++)
//                        for (int x = 0; x < length; x++) {
//                            if (model.getElement(x, z, i) == null) {
//                                BlockUtil.remove(loc.clone().add(-x, 0, -z));
//                            }
//                            else if (!model.getElement(x, z, i).equals("*")){
//                                BlockUtil.replace(loc.clone().add(-x, 0, -z), model.getElement(x, z, i));
//                            }
//                        }
//                }
//                break;
//            case 3:
//                for (int i = 0; i < height; i++) {
//                    //起点定于左上角，向右下遍历
//                    Location loc = startLoc.clone().add(-zOffset, i, xOffset);
//                    for (int z = 0; z < width; z++)
//                        for (int x = 0; x < length; x++) {
//                            if (model.getElement(x, z, i) == null) {
//                                BlockUtil.remove(loc.clone().add(z, 0, -x));
//                            }
//                            else if (!model.getElement(x, z, i).equals("*")){
//                                BlockUtil.replace(loc.clone().add(z, 0, -x), model.getElement(x, z, i));
//                            }
//                        }
//                }
//                break;
//            case 4:
//                for (int i = 0; i < height; i++) {
//                    //起点定于右下角，向左上遍历
//                    Location loc = startLoc.clone().add(zOffset, i, -xOffset);
//                    for (int z = 0; z < width; z++)
//                        for (int x = 0; x < length; x++) {
//                            if (model.getElement(x, z, i) == null) {
//                                BlockUtil.remove(loc.clone().add(-z, 0, x));
//                            }
//                            else if (!model.getElement(x, z, i).equals("*")){
//                                BlockUtil.replace(loc.clone().add(-z, 0, x), model.getElement(x, z, i));
//                            }
//                        }
//                }
//                break;
//            case 5:
//                for (int i = 0; i < height; i++) {
//                    //起点定于左下角（镜像），向上左遍历
//                    Location loc = startLoc.clone().add(-zOffset, i, -xOffset);
//                    for (int z = 0; z < width; z++)
//                        for (int x = 0; x < length; x++) {
//                            if (model.getElement(x, z, i) == null) {
//                                BlockUtil.remove(loc.clone().add(z, 0, x));
//                            }
//                            else if (!model.getElement(x, z, i).equals("*")){
//                                BlockUtil.replace(loc.clone().add(z, 0, x), model.getElement(x, z, i));
//                            }
//                        }
//                }
//                break;
//            case 6:
//                for (int i = 0; i < height; i++) {
//                    //起点定于右上角（镜像），向下左遍历
//                    Location loc = startLoc.clone().add(zOffset, i, xOffset);
//                    for (int z = 0; z < width; z++)
//                        for (int x = 0; x < length; x++) {
//                            if (model.getElement(x, z, i) == null) {
//                                BlockUtil.remove(loc.clone().add(-z, 0, -x));
//                            }
//                            else if (!model.getElement(x, z, i).equals("*")){
//                                BlockUtil.replace(loc.clone().add(-z, 0, -x), model.getElement(x, z, i));
//                            }
//                        }
//                }
//                break;
//            case 7:
//                for (int i = 0; i < height; i++) {
//                    //起点定于左上角（镜像)，向右下遍历
//                    Location loc = startLoc.clone().add(-xOffset, i, zOffset);
//                    for (int z = 0; z < width; z++)
//                        for (int x = 0; x < length; x++) {
//                            if (model.getElement(x, z, i) == null) {
//                                BlockUtil.remove(loc.clone().add(x, 0, -z));
//                            }
//                            else if (!model.getElement(x, z, i).equals("*")){
//                                BlockUtil.replace(loc.clone().add(x, 0, -z), model.getElement(x, z, i));
//                            }
//                        }
//                }
//                break;
//            case 8:
//                for (int i = 0; i < height; i++) {
//                    //起点定于右下角（镜像），向左上遍历
//                    Location loc = startLoc.clone().add(xOffset, i, -zOffset);
//                    for (int z = 0; z < width; z++)
//                        for (int x = 0; x < length; x++) {
//                            if (model.getElement(x, z, i) == null) {
//                                BlockUtil.remove(loc.clone().add(-x, 0, z));
//                            }
//                            else if (!model.getElement(x, z, i).equals("*")){
//                                BlockUtil.replace(loc.clone().add(-x, 0, z), model.getElement(x, z, i));
//                            }
//                        }
//                }
//                break;
//        }
//    }
//}

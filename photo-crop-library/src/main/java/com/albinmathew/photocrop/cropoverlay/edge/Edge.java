/*******************************************************************************
 * Copyright  2015 Albin Mathew.
 *  <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.albinmathew.photocrop.cropoverlay.edge;

/**
 * Enum representing an edge in the crop window.
 */
public enum Edge {

    LEFT,
    TOP,
    RIGHT,
    BOTTOM;


    private float mCoordinate;


    /**
     * Gets the current width of the crop window.
     */
    public static float getWidth() {
        return Edge.RIGHT.getCoordinate() - Edge.LEFT.getCoordinate();
    }

    /**
     * Gets the current height of the crop window.
     */
    public static float getHeight() {
        return Edge.BOTTOM.getCoordinate() - Edge.TOP.getCoordinate();
    }

    /**
     * Gets the coordinate of the Edge
     *
     * @return the Edge coordinate (x-coordinate for LEFT and RIGHT Edges and
     * the y-coordinate for TOP and BOTTOM edges)
     */
    public float getCoordinate() {
        return mCoordinate;
    }

    /**
     * Sets the coordinate of the Edge. The coordinate will represent the
     * x-coordinate for LEFT and RIGHT Edges and the y-coordinate for TOP and
     * BOTTOM edges.
     *
     * @param coordinate the position of the edge
     */
    public void setCoordinate(float coordinate) {
        mCoordinate = coordinate;
    }

}

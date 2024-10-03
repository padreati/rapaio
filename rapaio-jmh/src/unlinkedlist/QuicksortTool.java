/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package unlinkedlist;

import java.util.Arrays;
import java.util.stream.Collectors;

public class QuicksortTool {

    void quicksort(int[] array, int from, int to) {
        int size = to - from;
        if (size <= 1) {
            return;
        }
        int pivot = partition(array, from, to);
        quicksort(array, from, pivot);
        quicksort(array, pivot + 1, to);
    }

    int partition(int[] array, int from, int to) {
        int pivot = array[to - 1];
        int boundary = partition(array, pivot, from, to - 1);
        int temp = array[boundary];
        array[to - 1] = temp;
        array[boundary] = pivot;
        return boundary;
    }

    int partition(int[] array, int pivot, int lowerPointer, int upperPointer) {
        while (lowerPointer < upperPointer) {
            if (array[lowerPointer] < pivot) {
                lowerPointer++;
            } else if (array[upperPointer - 1] >= pivot) {
                upperPointer--;
            } else {
                int tmp = array[lowerPointer];
                int index = --upperPointer;
                array[lowerPointer++] = array[index];
                array[index] = tmp;
            }
        }
        return lowerPointer;
    }

    public static void main(String[] args) {
        int[] array = new int[10];
        new QuicksortTool().quicksort(array, 0, array.length);
        System.out.println(Arrays.stream(array).mapToObj(String::valueOf).collect(Collectors.joining(",")));
    }
}

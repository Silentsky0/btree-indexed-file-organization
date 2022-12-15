package dev.silentsky.disk;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Index {

    @Getter
    @Setter
    public Integer key;

    @Setter
    @Getter
    public Integer dataPointer;

    @Override
    public String toString() {
        return "Index{" +
                "key=" + key +
                ", dataPointer=" + dataPointer +
                '}';
    }

//    @Override
//    public int compareTo(Index i) {
//        return key.compareTo(i.key);
//    }
}

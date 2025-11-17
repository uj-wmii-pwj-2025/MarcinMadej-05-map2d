package uj.wmii.pwj.map2d;

import java.util.*;
import java.util.function.Function;

public class Map2D_Imp<R,C,V> implements Map2D<R,C,V>{

    private final Map<R, Map<C,V>> map2d = new HashMap<>();

    @Override
    public V put(R rowKey, C columnKey, V value) {
        Objects.requireNonNull(rowKey);
        Objects.requireNonNull(columnKey);
        return map2d.computeIfAbsent(rowKey, k -> new HashMap<>()).put(columnKey,value);
    }

    @Override
    public V get(R rowKey, C columnKey) {
        if(map2d.get(rowKey) == null){
            return null;
        } else {
            return map2d.get(rowKey).get(columnKey);
        }
    }

    @Override
    public V getOrDefault(R rowKey, C columnKey, V defaultValue) {
        if(map2d.get(rowKey) == null){
            return defaultValue;
        } else {
            return map2d.get(rowKey).getOrDefault(columnKey, defaultValue);
        }
    }

    @Override
    public V remove(R rowKey, C columnKey) {
        if(map2d.get(rowKey) == null){
            return null;
        }
        V toReturn = map2d.get(rowKey).remove(columnKey);
        if(map2d.get(rowKey).isEmpty()){
           map2d.remove(rowKey);
        }
        return  toReturn;
    }

    @Override
    public boolean isEmpty() {
        return map2d.isEmpty();
    }

    @Override
    public boolean nonEmpty() {
        return !map2d.isEmpty();
    }

    @Override
    public int size() {
        int count = 0;
        for (Map<C,V> rowMap : map2d.values()) {
            count += rowMap.size();
        }
        return count;
    }

    @Override
    public void clear() {
        map2d.clear();
    }

    @Override
    public Map<C, V> rowView(R rowKey) {
        if (map2d.get(rowKey) == null){
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap(map2d.get(rowKey));
        }
    }

    @Override
    public Map<R, V> columnView(C columnKey) {
        Map<R,V> result = new HashMap<>();
        map2d.forEach((rowKey, rowMap) -> {
            if(rowMap.containsKey(columnKey)){
                result.put(rowKey,rowMap.get(columnKey));
            }
        });
        return Collections.unmodifiableMap(result);

    }

    @Override
    public boolean containsValue(V value) {
        return map2d.values().stream().anyMatch(rowMap ->
                rowMap.containsValue(value)
        );
    }

    @Override
    public boolean containsKey(R rowKey, C columnKey) {
        if(map2d.get(rowKey) == null){
            return false;
        } else {
            return map2d.get(rowKey).containsKey(columnKey);
        }
    }

    @Override
    public boolean containsRow(R rowKey) {
        if(map2d.get(rowKey) == null){
            return false;
        } else {
            return !map2d.get(rowKey).isEmpty();
        }
    }

    @Override
    public boolean containsColumn(C columnKey) {
        return map2d.values().stream().anyMatch(
                rowMap -> rowMap.containsKey(columnKey)
        );
    }

    @Override
    public Map<R, Map<C, V>> rowMapView() {
        Map<R, Map<C, V>> copy = new HashMap<>();
        map2d.forEach((rowKey, rowMap) ->
            copy.put(rowKey, Map.copyOf(rowMap))
        );
        return Collections.unmodifiableMap(copy);
    }

    @Override
    public Map<C, Map<R, V>> columnMapView() {
        Map<C, Map<R, V>> result = new HashMap<>();

        map2d.forEach((rowKey, rowMap) ->
            rowMap.forEach((colKey, value) ->
                result.computeIfAbsent(
                    colKey,
                    newColumn -> new HashMap<>()).put(rowKey, value)
                )
        );

        result.replaceAll((c, colMap) -> Collections.unmodifiableMap(colMap));
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map2D<R, C, V> fillMapFromRow(Map<? super C, ? super V> target, R rowKey) {
        Map<C,V> rowMap = map2d.get(rowKey);
        if(rowMap != null){
            target.putAll(rowMap);
        }
        return this;
    }

    @Override
    public Map2D<R, C, V> fillMapFromColumn(Map<? super R, ? super V> target, C columnKey) {
        map2d.forEach((rowKey, rowMap) -> {
            if (rowMap.containsKey(columnKey)) {
                target.put(rowKey, rowMap.get(columnKey));
            }
        });
        return this;
    }

    @Override
    public Map2D<R, C, V> putAll(Map2D<? extends R, ? extends C, ? extends V> source) {
        source.rowMapView().forEach((rowKey, rowMap) ->
            rowMap.forEach((colKey, value) ->
                   this.put(rowKey, colKey, value)
            )
        );
        return this;
    }

    @Override
    public Map2D<R, C, V> putAllToRow(Map<? extends C, ? extends V> source, R rowKey) {
        source.forEach((colKey,value) ->
            this.put(rowKey, colKey, value)
        );
        return this;
    }

    @Override
    public Map2D<R, C, V> putAllToColumn(Map<? extends R, ? extends V> source, C columnKey) {
        source.forEach((rowKey, value) ->
            this.put(rowKey, columnKey, value)
        );
        return this;
    }

    @Override
    public <R2, C2, V2> Map2D<R2, C2, V2> copyWithConversion(Function<? super R, ? extends R2> rowFunction, Function<? super C, ? extends C2> columnFunction, Function<? super V, ? extends V2> valueFunction) {

        Map2D<R2, C2, V2> result = Map2D.createInstance();
        this.rowMapView().forEach((row, rowMap) -> {

            R2 newRowKey = rowFunction.apply(row);
            rowMap.forEach((col, val) ->{

               C2 newColKey = columnFunction.apply(col);
               V2 newVal = valueFunction.apply(val);
               result.put(newRowKey, newColKey, newVal);

            });
        });
        return result;

    }
}

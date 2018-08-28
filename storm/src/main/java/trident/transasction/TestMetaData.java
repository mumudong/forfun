package trident.transasction;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/5/30.
 */
public class TestMetaData implements Serializable {

    private static final long serialVersionUID = 1L;

    private long _index;
    private long _size;

    public long get_index() {
        return _index;
    }
    public void set_index(long _index) {
        this._index = _index;
    }

    public long get_size() {
        return _size;
    }
    public void set_size(long _size) {
        this._size = _size;
    }

    @Override
    public String toString() {
        return "[_index=" + _index + ", _size=" + _size + "]";
    }
}

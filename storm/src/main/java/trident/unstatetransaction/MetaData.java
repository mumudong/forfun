package trident.unstatetransaction;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/5/30.
 */
public class MetaData implements Serializable {

    private static final long serialVersionUID = 1L;

    private int _index = 0;
    private int _size;

    public int get_index() {
        return _index;
    }
    public void set_index(int _index) {
        this._index = _index;
    }

    public int get_size() {
        return _size;
    }
    public void set_size(int _size) {
        this._size = _size;
    }

    @Override
    public String toString() {
        return "[_index=" + _index + ", _size=" + _size + "]";
    }
}

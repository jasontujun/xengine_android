package com.xengine.android.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * 调用WeakReference内容T的==运算符进行比较。
 * 覆盖了WeakReference的equals和hashCode方法，以实现“值相等”的比较。
 * 主要用在Collection中，可以直接使用remove等操作来管理值相等的对象。
 * 由于Reference和WeakReference都没有覆盖这两个方法，故无法实现上述需求。
 * Created with IntelliJ IDEA.
 * User: jasontujun
 * Date: 13-9-22
 * Time: 下午3:54
 * To change this template use File | Settings | File Templates.
 */
public class XWeakEqualityReference<T> extends WeakReference<T> {
    public XWeakEqualityReference(T r) {
        super(r);
    }

    public XWeakEqualityReference(T r, ReferenceQueue<? super T> q) {
        super(r, q);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other) {

        boolean returnValue = super.equals(other);

        // If we're not equal, then check equality using referenced objects
        if (!returnValue && (other instanceof XWeakEqualityReference<?>)) {
            T value = this.get();
            if (null != value) {
                T otherValue = ((XWeakEqualityReference<T>) other).get();

                // The delegate equals should handle otherValue == null
                returnValue = value.equals(otherValue);
            }
        }

        return returnValue;
    }

    @Override
    public int hashCode() {
        T value = this.get();
        return value != null ? value.hashCode() : super.hashCode();
    }
}

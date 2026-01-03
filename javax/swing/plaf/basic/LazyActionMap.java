/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ActionMapUIResource;

class LazyActionMap
extends ActionMapUIResource {
    private transient Object _loader;

    static void installLazyActionMap(JComponent c, Class<?> loaderClass, String defaultsKey) {
        ActionMap map = (ActionMap)UIManager.get(defaultsKey);
        if (map == null) {
            map = new LazyActionMap(loaderClass);
            UIManager.getLookAndFeelDefaults().put(defaultsKey, map);
        }
        SwingUtilities.replaceUIActionMap(c, map);
    }

    static ActionMap getActionMap(Class<?> loaderClass, String defaultsKey) {
        ActionMap map = (ActionMap)UIManager.get(defaultsKey);
        if (map == null) {
            map = new LazyActionMap(loaderClass);
            UIManager.getLookAndFeelDefaults().put(defaultsKey, map);
        }
        return map;
    }

    private LazyActionMap(Class<?> loader) {
        this._loader = loader;
    }

    public void put(Action action) {
        this.put(action.getValue("Name"), action);
    }

    @Override
    public void put(Object key, Action action) {
        this.loadIfNecessary();
        super.put(key, action);
    }

    @Override
    public Action get(Object key) {
        this.loadIfNecessary();
        return super.get(key);
    }

    @Override
    public void remove(Object key) {
        this.loadIfNecessary();
        super.remove(key);
    }

    @Override
    public void clear() {
        this.loadIfNecessary();
        super.clear();
    }

    @Override
    public Object[] keys() {
        this.loadIfNecessary();
        return super.keys();
    }

    @Override
    public int size() {
        this.loadIfNecessary();
        return super.size();
    }

    @Override
    public Object[] allKeys() {
        this.loadIfNecessary();
        return super.allKeys();
    }

    @Override
    public void setParent(ActionMap map) {
        this.loadIfNecessary();
        super.setParent(map);
    }

    private void loadIfNecessary() {
        block5: {
            if (this._loader != null) {
                Object loader = this._loader;
                this._loader = null;
                Class klass = (Class)loader;
                try {
                    Method method = klass.getDeclaredMethod("loadActionMap", LazyActionMap.class);
                    method.invoke((Object)klass, this);
                }
                catch (NoSuchMethodException nsme) {
                    assert (false) : "LazyActionMap unable to load actions " + String.valueOf(klass);
                }
                catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    if ($assertionsDisabled) break block5;
                    throw new AssertionError((Object)("LazyActionMap unable to load actions " + String.valueOf(e)));
                }
            }
        }
    }
}


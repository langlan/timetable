package com.jytec.cs.dao.common;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface Dao {
	/**
	 * searching by condition(optional) e.g.
	 * 
	 * <pre>
	 * List&lt;A&gt; items = dao.find("Select a From A");
	 * <b>or with any conditions :</b>
	 * String ql = "Select b From B b Where b.age=? And b.name Like ?";
	 * List&lt;B&gt; items = dao.find(ql, age, name);
	 * </pre>
	 * 
	 * @param <T> For convenience, result could be assigned to List&lt;AnyType&gt;, Thanks to Generic. ...
	 * @see #findPage(String, int, int, Object...)
	 */
	<T> List<T> find(String ql, Object... vars);

	<T> T get(Class<T> clazz, Serializable id);

	List<Map<String, Object>> findMaps(String string, Object... vars);

	<T> T save(T d);
}

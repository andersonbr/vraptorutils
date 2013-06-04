package br.com.shellcode.vraptorutils.jpa;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPAUtil {
	private static final Logger log = LoggerFactory.getLogger(JPAUtil.class);

	public enum OP {
		EQUAL("EQUAL"), NOTEQUAL("NOTEQUAL"), LIKE("LIKE"), ILIKE("ILIKE"),
		IN("IN"), NOTIN("NOTIN"), NOTLIKE("NOTLIKE"), NOTILIKE("NOTILIKE"),
		BETWEEN("BETWEEN"), ISNULL("ISNULL"), ISNOTNULL("ISNOTNULL"), LE("LE"),
		LT("LT"), GE("GE"), GT("GT");

		OP(String s) {
			this.value = s;
		}

		private String value;

		@Override
		public String toString() {
			return this.value;
		}
	}

	public enum OR {
		ASC("ASC"), DESC("DESC");

		OR(String s) {
			this.value = s;
		}

		private String value;

		@Override
		public String toString() {
			return this.value;
		}
	}

	/**
	 * Criteria builder, receiving Class, Entity Manager, instance with filled
	 * form fields and map with params and operation in search criteria
	 * 
	 * @author anderson
	 * 
	 * @param clazz
	 * @param entityManager
	 * @param instance
	 * @param filterMap
	 * @return
	 */
	public static <T> CriteriaQuery<Long> getCriteriaCount(Class<T> clazz,
			CriteriaBuilder cb, Object instance, Map<String, OP> filterMap,
			List<String> group) {

		/**
		 * Query result
		 */
		CriteriaQuery<Long> q = cb.createQuery(Long.class);

		/**
		 * From
		 */
		Root<T> c = q.from(clazz);

		/**
		 * Predicates
		 */
		Predicate filters = getPredicates(c, clazz, cb, instance, filterMap,
				null);
		if (filters != null)
			q.where(filters);

		/**
		 * Count
		 */
		q.select(cb.count(c));

		/**
		 * Group
		 */
		if (group != null && group.size() > 0) {
			List<Expression<?>> listGroups = getGroups(c, group);
			q.groupBy(listGroups);
		}
		return q;
	}

	public static <T> CriteriaQuery<T> getCriteriaQuery(Class<T> from,
			CriteriaBuilder cb, Object instance, Map<String, OP> filterMap,
			Map<String, OR> sort, List<String> group) {
		return getCriteriaQuery(from, from, cb, instance, filterMap, sort,
				group);
	}

	public static <S, T> CriteriaQuery<S> getCriteriaQuery(Class<S> get,
			Class<T> from, CriteriaBuilder cb, Object instance,
			Map<String, OP> filterMap, Map<String, OR> sort, List<String> group) {
		if (get != null && from != null) {

			/**
			 * Query result
			 */
			CriteriaQuery<S> q = cb.createQuery(get);

			/**
			 * From
			 */
			Root<T> root = q.from(from);

			/**
			 * Predicates
			 */
			Predicate filters = getPredicates(root, from, cb, instance,
					filterMap, sort);
			if (filters != null)
				q.where(filters);

			/**
			 * Group
			 */
			if (group != null && group.size() > 0) {
				List<Expression<?>> listGroups = getGroups(root, group);
				q.groupBy(listGroups);
			}

			/**
			 * Order
			 */
			if (sort != null && sort.size() > 0) {
				List<Order> listorders = getOrders(cb, root, sort);
				if (listorders != null)
					q.orderBy(listorders);
			}

			/**
			 * fields
			 */
			/**
			 * base criteria
			 */
			if (group != null) {
				/**
				 * selecting grouped fields and aggregation functions
				 */
				selectGroupedFields(cb, q, root, group);
			}
			return q;
		}
		return null;
	}

	private static <T> List<Expression<?>> getGroups(Root<T> root,
			List<String> group) {
		List<Expression<?>> groupRet = new ArrayList<Expression<?>>();
		if (group != null) {
			for (String field : group) {
				try {
					/**
					 * separate field and function
					 */
					String[] splitedfield = field.split(",");
					Path<String> path = getPath(root, splitedfield[0]);
					if (path != null) {
						groupRet.add(path);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return groupRet;
		}
		return null;
	}

	public static <T, S> void selectGroupedFields(CriteriaBuilder cb,
			CriteriaQuery<S> criteria, Root<T> root, List<String> group) {
		List<Selection<?>> listSelection = new ArrayList<Selection<?>>();
		if (group != null) {
			for (String currentVal : group) {
				/**
				 * separate field and function
				 */
				String[] splitCurrentVal = currentVal.split(",");
				Path<String> path = getPath(root, splitCurrentVal[0]);

				if (path != null) {
					/**
					 * if have aggregation function
					 */
					if (splitCurrentVal.length == 2) {
						/**
						 * aggregation functions
						 */
						Expression<?> expr = getExpressionPathAndFunctions(cb,
								path, splitCurrentVal[0], splitCurrentVal[1]);
						if (expr != null) {
							listSelection.add(expr);
						}
					}
					listSelection.add(path);
				}
			}
		}
		criteria.multiselect(listSelection);
	}

	private static Expression<?> getExpressionPathAndFunctions(
			CriteriaBuilder cb, Path<String> pathString, String element, String function) {
		Expression<?> expr = null;
		Path<Number> pNumber = getNumberPath(pathString, element);
		if (function.equalsIgnoreCase("count")) {
			/**
			 * field that can be numeric or not
			 */
			expr = cb.count(pathString);
		} else if (function.equalsIgnoreCase("sum")) {
			expr = cb.sum(pNumber);
		} else if (function.equalsIgnoreCase("avg")) {
			expr = cb.avg(pNumber);
		} else if (function.equalsIgnoreCase("min")) {
			expr = cb.min(pNumber);
		} else if (function.equalsIgnoreCase("max")) {
			expr = cb.max(pNumber);
		}
		return expr;
	}

	private static <T> Path<String> getPath(Root<T> root, String path) {
		Path<String> p = null;
		try {
			if (path != null) {
				String[] splitedpath = path.split("\\.");
				p = root.get(splitedpath[0]);
				for (int z = 1; z < splitedpath.length; z++) {
					p = p.get(splitedpath[z]);
				}
			}
		} catch (Exception e) {
			log.error("error when get path: " + path + " from "
					+ root.getJavaType());
		}
		return p;
	}

	private static Path<Number> getNumberPath(Path<String> p, String element) {
		if (Number.class.isAssignableFrom(p.getJavaType())) {
			return p.getParentPath().get(element);
		}
		return null;
	}

	/**
	 * Get list of fields orders
	 * 
	 * @param cb
	 * @param root
	 * @param sort
	 * @return
	 */
	private static <T> List<Order> getOrders(CriteriaBuilder cb, Root<T> root,
			Map<String, OR> sort) {
		List<Order> orders = new ArrayList<Order>();
		if (sort != null) {
			for (String field : sort.keySet()) {
				OR way = sort.get(field);
				try {
					Order or = null;

					/**
					 * separate field and function
					 */
					String[] splitedfield = field.split(",");
					Path<String> path = getPath(root, splitedfield[0]);
					if (path != null) {
						Expression<?> p = path;
						if (splitedfield.length == 2) {
							Expression<?> expr = getExpressionPathAndFunctions(
									cb, path, splitedfield[0], splitedfield[1]);
							if (expr != null) {
								p = expr;
							}
						}
						or = (way.equals(OR.ASC)) ? cb.asc(p) : cb.desc(p);
						orders.add(or);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return orders;
		}
		return null;
	}

	/**
	 * filter operations
	 * 
	 * @param root
	 * @param from
	 * @param cb
	 * @param instance
	 * @param filterMap
	 * @param sort
	 * @return
	 */
	private static <S, T> Predicate getPredicates(Root<S> root, Class<T> from,
			CriteriaBuilder cb, Object instance, Map<String, OP> filterMap,
			Map<String, OR> sort) {
		Predicate filters = null;
		Method[] methods = from.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			try {
				Method method = methods[i];
				if (from.isAssignableFrom(method.getDeclaringClass())
						&& method.getName().startsWith("get")
						&& method.getName().length() > 3
						&& method.getAnnotation(Transient.class) == null) {
					String attribute = Character.toLowerCase(method.getName()
							.charAt(3)) + method.getName().substring(4);
					Object value = method.invoke(instance);
					OP operation = OP.EQUAL;
					if (value != null) {
						/**
						 * defining operation by filter Map
						 */
						Predicate p = null;
						if (filterMap != null
								&& filterMap.get(attribute) != null) {
							operation = filterMap.get(attribute);
						}

						Path<Number> numberPath = null;
						Number nvalue = null;
						if (value instanceof Number) { 
							numberPath = root.get(attribute);
							nvalue = (Number) value;
						}

						if (operation.equals(OP.EQUAL)) {
							p = cb.equal(root.get(attribute), value);
						} else if (operation.equals(OP.NOTEQUAL)) {
							p = cb.notEqual(root.get(attribute), value);
						} else if (operation.equals(OP.ILIKE)
								&& value instanceof String) {
							p = cb.like(cb.lower(root.<String> get(attribute)),
									"%" + ((String) value).toLowerCase() + "%");
						} else if (operation.equals(OP.LIKE)
								&& value instanceof String) {
							p = cb.like(root.<String> get(attribute), "%"
									+ ((String) value) + "%");
						} else if (operation.equals(OP.NOTILIKE)
								&& value instanceof String) {
							p = cb.notLike(
									cb.lower(root.<String> get(attribute)), "%"
											+ ((String) value).toLowerCase()
											+ "%");
						} else if (operation.equals(OP.NOTLIKE)
								&& value instanceof String) {
							p = cb.notLike(root.<String> get(attribute), "%"
									+ ((String) value) + "%");
						} else if (operation.equals(OP.IN)) {
							p = root.get(attribute).in(value);
						} else if (operation.equals(OP.NOTIN)) {
							p = cb.not(root.get(attribute).in(value));
						} else if (operation.equals(OP.BETWEEN)) {
							// TODO: forma de receber dois valores
						} else if (operation.equals(OP.ISNULL)) {
							p = cb.isNull(root.get(attribute));
						} else if (operation.equals(OP.ISNOTNULL)) {
							p = cb.isNotNull(root.get(attribute));
						} else if (operation.equals(OP.LE)
								&& value instanceof Number) {
							p = cb.le(numberPath, nvalue);
						} else if (operation.equals(OP.LT)
								&& value instanceof Number) {
							p = cb.lt(numberPath, nvalue);
						} else if (operation.equals(OP.GE)
								&& value instanceof Number) {
							p = cb.ge(numberPath, nvalue);
						} else if (operation.equals(OP.GT)
								&& value instanceof Number) {
							p = cb.gt(numberPath, nvalue);
						}

						if (p != null)
							filters = (filters != null) ? cb.and(filters, p)
									: p;
					}
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return filters;
	}
}
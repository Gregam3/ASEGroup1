package asegroup1.api.services;

import asegroup1.api.daos.Dao;

import java.util.List;

/**
 * @author Greg Mitten 
 * gregoryamitten@gmail.com
 */

public class ServiceImpl<T> {
	private Dao<T> dao;

	public ServiceImpl(Dao<T> dao) {
		this.dao = dao;
	}

	public T get(String id) {
		checkIfDaoIsValid();
		return dao.get(id);
	}

	public void update(T t) {
		checkIfDaoIsValid();
		dao.update(t);
	}

	public void delete(String id) {
		checkIfDaoIsValid();
		dao.delete(id);
	}

	public List<T> list() {
		checkIfDaoIsValid();
		return dao.list();
	}

	public void create(T t) {
		checkIfDaoIsValid();
		dao.add(t);
	}

	private void checkIfDaoIsValid() {
		if (dao == null) {
			throw new UnsupportedOperationException("In order to use this method you must set the class in constructor. " +
					"E.g. for UserServiceImpl extends ServiceImpl<User>, you should put super(userDao) in your constructor." +
					"If userDao does not yet exist you must create it and then @Autowire it to give it its state");
		}
	}
}

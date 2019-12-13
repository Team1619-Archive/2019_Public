package org.team1619.utilities.eventbus;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

public class AsyncEventBus extends EventBus {

	private final Executor fExecutor;

	private final Set<Object> fListeners;

	public AsyncEventBus(Executor executor) {
		fExecutor = executor;

		fListeners = Collections.synchronizedSet(new HashSet<>());
	}

	@Override
	public void register(Object listener) {
		fListeners.add(listener);
	}

	@Override
	public void unregister(Object listener) {
		fListeners.remove(listener);
	}

	@Override
	public void post(Object event) {
		for (Object listener : fListeners) {
			for (Method method : listener.getClass().getMethods()) {
				Class[] parameters = method.getParameterTypes();
				if (parameters.length == 1 && parameters[0].equals(event.getClass())) {
					for (Annotation annotation : method.getDeclaredAnnotations()) {
						if (annotation.annotationType().equals(Subscribe.class)) {
							fExecutor.execute(() -> {
								try {
									method.invoke(listener, event);
								} catch (IllegalAccessException e) {
									e.printStackTrace();
								} catch (InvocationTargetException e) {
									e.getCause().printStackTrace();
								}
							});
							break;
						}
					}
				}
			}
		}
	}
}

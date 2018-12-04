package com.riekr.mame.utils;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;

public final class DelayableTask implements Runnable {

	private final @NotNull Runnable _task;
	private final long _delay;
	private final AtomicLong _timeLimit = new AtomicLong();

	public DelayableTask(long delay, @NotNull Runnable task) {
		if (delay <= 0)
			throw new IllegalArgumentException("Wrong delay");
		_delay = delay;
		_task = task;
	}

	public void touch() {
		final long t = _timeLimit.getAndSet(System.currentTimeMillis() + _delay);
		if (t == 0L) {
			Thread thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}
	}

	@Override
	public void run() {
		try {
			long now, limit;
			do {
				while ((now = System.currentTimeMillis()) < (limit = _timeLimit.get())) {
					Thread.sleep(limit - now);
				}
				_task.run();
			} while (_timeLimit.compareAndSet(limit, 0L));
		} catch (InterruptedException ignored) {
		}
	}
}

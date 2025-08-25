package org.nic.auth.common;

import org.nic.auth.config.base.IConfigReader;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ThreadTimer {
    public static ConcurrentHashMap<String, ThreadTimer> gethreadTimerPool() {
        return threadTimerPool;
    }

    public static List<BaseCaller> gethisCallers() {
        return hisCallers;
    }

    private static ConcurrentHashMap<String, ThreadTimer> threadTimerPool = new ConcurrentHashMap<>();
    private static List<BaseCaller> hisCallers = new ArrayList<>();

    public BaseCaller caller;
    private int dueTime;
    private int period;

    public void setDueTime(int dueTime) {
        this.dueTime = dueTime;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    private Thread tmain;

    private IConfigReader configReader;

    private ThreadTimer(BaseCaller caller) {
        configReader = ReflectInternalUtil.getBean(IConfigReader.class);
        this.caller = caller;
        this.dueTime = 0;
        this.period = -1;
    }

    private ThreadTimer(BaseCaller caller, int dueTime, int period) {
        configReader = ReflectInternalUtil.getBean(IConfigReader.class);
        this.caller = caller;
        this.dueTime = dueTime;
        this.period = period;
        if (this.caller != null) {
            this.caller.setThreadType(1);
        }
    }

    public static ThreadTimer run(SampleCaller caller) throws InterruptedException {
        ThreadTimer timer = getTimer(null, new BaseCaller() {
            @Override
            public void call(Object o) {
                if (caller != null) {
                    caller.accept();
                }
            }
        });
        if (timer == null) {
            return null;
        }
        return timer.start("", null);
    }

    public static <T> ThreadTimer run(Consumer<T> caller, T param) throws InterruptedException {
        ThreadTimer timer = getTimer(null, new BaseCaller() {
            @Override
            public void call(Object o) {
                if (caller != null) {
                    caller.accept(param);
                }
            }
        });
        if (timer == null) {
            return null;
        }
        return timer.start("", null);
    }

    public static ThreadTimer run(BaseCaller caller) throws InterruptedException {
        ThreadTimer timer = getTimer(null, caller);
        if (timer == null) {
            return null;
        }
        return timer.start("", null);
    }

    public static ThreadTimer run(BaseCaller caller, int dueTime, int period) throws InterruptedException {
        ThreadTimer timer = getTimer(null, caller);
        timer.setDueTime(dueTime);
        timer.setPeriod(period);
        if (timer == null) {
            return null;
        }
        return timer.start("", null);
    }

    public static ThreadTimer run(String threadName, BaseCaller caller) throws InterruptedException {
        ThreadTimer timer = getTimer(threadName, caller);
        if (timer == null) {
            return null;
        }
        return timer.start(threadName, null);
    }

    public static ThreadTimer run(String threadName, BaseCaller caller, int dueTime, int period) throws InterruptedException {
        ThreadTimer timer = getTimer(threadName, caller);
        timer.setDueTime(dueTime);
        timer.setPeriod(period);
        if (timer == null) {
            return null;
        }
        return timer.start(threadName, null);
    }

    public static <TParam> ThreadTimer run(BaseCaller caller, TParam param) throws InterruptedException {
        ThreadTimer timer = getTimer(null, caller);
        if (timer == null) {
            return null;
        }
        return timer.start("", param);
    }

    public static <TParam> ThreadTimer run(BaseCaller caller, int dueTime, int period, TParam param) throws InterruptedException {
        ThreadTimer timer = new ThreadTimer(caller, dueTime, period);
        return timer.start("", param);
    }

    public static <TParam> ThreadTimer run(String threadName, BaseCaller caller, TParam param) throws InterruptedException {
        ThreadTimer timer = getTimer(threadName, caller);
        if (timer == null) {
            return null;
        }
        return timer.start(threadName, param);
    }

    public static <TParam> ThreadTimer run(String threadName, BaseCaller caller, int dueTime, int period, TParam param) throws InterruptedException {
        ThreadTimer timer = getTimer(threadName, caller);
        timer.setDueTime(dueTime);
        timer.setPeriod(period);
        if (timer == null) {
            return null;
        }
        return timer.start(threadName, param);
    }

    private static ThreadTimer getTimer(String sid, BaseCaller caller) {
        if (DataConvert.isNullOrEmpty(sid)) {
            sid = UUID.randomUUID().toString();
        }
        ThreadTimer timer = null;
        if (!threadTimerPool.containsKey(caller.getId())) {
            timer = new ThreadTimer(caller);
        }

        //获取线程的名称信息，同一名称任务不允许启动多个
        if (caller.getId() == null || caller.getId().length() == 0) {
            if (!DataConvert.isNullOrEmpty(sid)) {
                caller.setName(sid);
                caller.setId(sid);
            }
        }

        return timer;
    }

    private <TParam> ThreadTimer start(String threadName, TParam param) throws InterruptedException {
        if (caller == null) {
            //DelegateForTools.call(ILogger.class, c -> ((ILogger) c).writeErrorLog("ThreadTimer", "start",
            //        String.format("线程caller不能为空")));
            return this;
        }

        if (DataConvert.isNullOrEmpty(caller.getId())) {
            String id = null;
            if (param != null) {
                try {
                    String jdata = null;
                    jdata = JsonConvert.toJson(param);
                    if (jdata != null) {
                        id = MD5Helper.getMd5MessageDigest(jdata.getBytes());
                    } else {
                        id = UUID.randomUUID().toString();
                    }
                } catch (Exception ex) {
                }
            } else {
                id = UUID.randomUUID().toString();
            }
            caller.setId(id);
            caller.setName(id);
        }
        //验证当前线程池
        if (threadTimerPool.containsKey(caller.getId())) {
            return this;
        }

        ScheduledExecutorService scheduledThreadPool = getScheduleThreadPool();
        if (period > 0) {
            ScheduledFuture scheduledFuture = scheduledThreadPool.scheduleWithFixedDelay(caller, dueTime, period, TimeUnit.MILLISECONDS);
            if (scheduledFuture != null) {
                threadTimerPool.put(caller.getId(), this);
            }
        } else {
            scheduledThreadPool.schedule(caller, dueTime, TimeUnit.MILLISECONDS);
        }
        return this;
    }

    public void stop() {
        if (caller != null) {
            if (caller.getId() != null && caller.getId().length() > 0) {
                if (threadTimerPool.containsKey(caller.getId())) {
                    threadTimerPool.remove(caller.getId());
                }
            }
            caller = null;
        }

        if (tmain != null) {
            tmain.stop();
            tmain = null;
        }
    }

    private static Hashtable<String, ScheduledExecutorService> singleThreadPools = new Hashtable<>();
    private static Object singleThreadPoolLocker = new Object();

    @PostConstruct
    private ScheduledExecutorService getScheduleThreadPool() {
        return getScheduleThreadPool(null);
    }

    private ScheduledExecutorService getScheduleThreadPool(String key) {
        ScheduledExecutorService singleThreadPool;
        boolean isUseSyncLocker = false;
        if (key == null) {
            isUseSyncLocker = true;
            key = "MySingleThreadPool";
        }
        if ((singleThreadPool = singleThreadPools.get(key)) == null) {
            if (isUseSyncLocker) {
                synchronized (singleThreadPoolLocker) {
                    singleThreadPool = getSchedulePool(key);
                }
            } else {
                singleThreadPool = getSchedulePool(key);
            }
        }
        return singleThreadPool;
    }

    private ScheduledExecutorService getSchedulePool(String key) {
        ScheduledExecutorService schedule = null;
        if ((schedule = singleThreadPools.get(key)) == null) {
            int cpus = 4;
            if (configReader != null) {
                cpus = configReader.get(SysCPUNumbers);
            }
            schedule = Executors.newScheduledThreadPool(2 * cpus);
            singleThreadPools.put(key, schedule);
        }
        return schedule;
    }

    public abstract static class BaseCaller<TParam> implements Runnable {
        @Override
        public void run() {
            setStartTime(System.currentTimeMillis());
            call(param);
            setEndTime(System.currentTimeMillis());

            if (threadType == 1) {
                synchronized (hisCallers) {
                    if (hisCallers != null) {
                        Stream<BaseCaller> callerStream = hisCallers.stream()
                                .filter(c -> c.getId().equals(this.id));
                        if (callerStream != null) {
                            List<BaseCaller> callers = callerStream.collect(Collectors.toList());
                            if (callers == null) {
                                hisCallers.add(this);
                            } else if (callers.size() < 10) {
                                hisCallers.add(this);
                            } else {
                                callers.remove(0);
                                hisCallers.add(this);
                            }
                        } else {
                            hisCallers.add(this);
                        }
                    }
                }
            }
        }

        private TParam param;

        public void setParam(TParam param) {
            this.param = param;
        }

        public abstract void call(TParam param);

        private String id = "";

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }


        private String name = "";

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private long startTime;

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        private long endTime;

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        private int threadType;

        public int getThreadType() {
            return threadType;
        }

        public void setThreadType(int threadType) {
            this.threadType = threadType;
        }

        private boolean runningStatus = false;

        public Boolean getIsRunning() {
            return this.runningStatus;
        }

        public void setIsRunning(boolean runingStatus) {
            this.runningStatus = runingStatus;
        }
    }
}


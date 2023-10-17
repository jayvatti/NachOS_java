// Scheduler.java
//	Class to choose the next thread to run.
//
// 	These routines assume that interrupts are already disabled.
//	If interrupts are disabled, we can assume mutual exclusion
//	(since we are on a uniprocessor).
//
// 	NOTE: We can't use Locks to provide mutual exclusion here, since
// 	if we needed to wait for a lock, and the lock was busy, we would 
//	end up calling FindNextToRun(), and that would put us in an 
//	infinite loop.
//
// 	Very simple implementation -- no priorities, straight FIFO.
//	Might need to be improved in later assignments.
//
// Copyright (c) 1992-1993 The Regents of the University of California.
// Copyright (c) 1998 Rice University.
// All rights reserved.  See the COPYRIGHT file for copyright notice and
// limitation of liability and disclaimer of warranty provisions.


//------------------------------------------------------------------------
// Create a handler for scheduled interrupts for Round Robin
// implementation.
//
// MP1
//------------------------------------------------------------------------
//class YourHandler

// - Round Robin Interrupt Handler
class SchedulerRR_Handler extends Thread {

    public SchedulerRR_Handler(){};

    @Override
    public void run() {
        if (Interrupt.getStatus() != Interrupt.IdleMode)
            Interrupt.yieldOnReturn();
    }
}
class Scheduler {

    static private List readyList;// queue of threads that are ready to run,
    // but not running

    //constants for scheduling policies
    static final int POLICY_PRIO_NP = 1;
    static final int POLICY_PRIO_P = 2;
    static final int POLICY_RR = 3;
    static final int POLICY_SJF_NP = 4;
    static final int POLICY_SJF_P = 5;
    static final int POLICY_FCFS = 6;

    static int policy=POLICY_FCFS;


    static public SchedulerRR_Handler RR_Handler;
    static public NachosThread threadToBeDestroyed;





    //----------------------------------------------------------------------
    // Scheduler
    // 	Initialize the list of ready but not running threads to empty.
    //----------------------------------------------------------------------

    static {
        readyList = new List();
    }

    //----------------------------------------------------------------------
    // start
    // 	called by a Java thread (usually the initial thread that calls
    //    Nachos.main). Starts the first Nachos thread.
    //
    //----------------------------------------------------------------------

    public static void start() {
        NachosThread nextThread;


        Debug.println('t', "Scheduling first Nachos thread");

        nextThread = findNextToRun();
        if (nextThread == null) {
            Debug.print('+', "Scheduler.start(): no NachosThread ready!");
            return;
        }

        Debug.println('t', "Switching to thread: " + nextThread.getName());

        // - creating RR_Handler - MP1
        if (policy == POLICY_RR) RR_Handler = new SchedulerRR_Handler();

        synchronized (nextThread) {
            nextThread.setStatus(NachosThread.RUNNING);
            nextThread.notify();
        }
        // nextThread is now running
    }

    //----------------------------------------------------------------------
    // readyToRun
    // 	Mark a thread as ready, but not running.
    //	Put it on the ready list, for later scheduling onto the CPU.
    //
    //	"thread" is the thread to be put on the ready list.
    //----------------------------------------------------------------------

    public static void readyToRun(NachosThread thread) {
        Debug.print('t', "Putting thread on ready list: " + thread.getName() +
                "\n");

        thread.setStatus(NachosThread.READY);

        //MP1
        // - switch case update
        switch (policy)
        {
            case POLICY_RR:
            case POLICY_FCFS:
                readyList.append(thread);
                break;
            case POLICY_SJF_P:
            case POLICY_SJF_NP:
                readyList.sortedInsert(thread, thread.timeLeft);
                break;
            case POLICY_PRIO_P:
            case POLICY_PRIO_NP:
                readyList.sortedInsert(thread, thread.priority);
                break;
        }
    }

    //----------------------------------------------------------------------
    // findNextToRun
    // 	Return the next thread to be scheduled onto the CPU.
    //	If there are no ready threads, return null.
    // Side effect:
    //	Thread is removed from the ready list.
    //----------------------------------------------------------------------

    public static NachosThread findNextToRun() {
        return (NachosThread)readyList.remove();
    }

    //----------------------------------------------------------------------
    // run
    // 	Dispatch the CPU to nextThread.  Save the state of the old thread,
    //	and load the state of the new thread, by calling the machine
    //	dependent context switch routine, SWITCH.
    //
    //      Note: we assume the state of the previously running thread has
    //	already been changed from running to blocked or ready (depending).
    // Side effect:
    //	The global variable currentThread becomes nextThread.
    //
    //	"nextThread" is the thread to be put into the CPU.
    //----------------------------------------------------------------------

    public static void run(NachosThread nextThread) {
        NachosThread oldThread;

        oldThread = NachosThread.thisThread();

        if (Nachos.USER_PROGRAM) {  // ignore until running user programs
            if (oldThread.space != null) {// if this thread runs a user program,
                oldThread.saveUserState(); // save the user's CPU registers
                oldThread.space.saveState();
            }
        }

        //MP1 Round Robin - schedule an interrupt if necessary
        if (policy == POLICY_RR && nextThread.timeLeft > 4) Interrupt.schedule(RR_Handler, 40, Interrupt.TimerInt );

        Debug.println('t', "Switching from thread: " + oldThread.getName() +
                " to thread: " + nextThread.getName());

        // We do this in Java via wait/notify of the underlying Java threads.

        synchronized (nextThread) {
            nextThread.setStatus(NachosThread.RUNNING);
            nextThread.notify();
        }
        synchronized (oldThread) {
            while (oldThread.getStatus() != NachosThread.RUNNING)
                try {oldThread.wait();} catch (InterruptedException e) {};
        }

        Debug.println('t', "Now in thread: " + NachosThread.thisThread().getName());

        // If the old thread gave up the processor because it was finishing,
        // we need to delete its carcass.
        if (threadToBeDestroyed != null) {
            threadToBeDestroyed.stop();
            threadToBeDestroyed = null;
        }

        if (Nachos.USER_PROGRAM) {
            if (oldThread.space != null) {// if there is an address space
                oldThread.restoreUserState();     // to restore, do it.
                oldThread.space.restoreState();
            }
        }
    }

    //----------------------------------------------------------------------
    // print
    // 	Print the scheduler state -- in other words, the contents of
    //	the ready list.  For debugging.
    //----------------------------------------------------------------------
    public static void print() {
        System.out.print("Ready list contents:");
        readyList.print();
    }

    public static void setSchedulerPolicy(int p) {policy = p;}

    //----------------------------------------------------------------------
    // shouldISwitch
    //    Checks to see if current thread should be preempted
    //----------------------------------------------------------------------


    // - shouldISwitch -> preempt thread for Priority Policy and Round Robin:
    public static boolean shouldISwitch(NachosThread current, NachosThread newThread)
    {
        //MP1 preemption code
        if(policy == POLICY_PRIO_P) return newThread.priority < current.priority;
        else if (policy == POLICY_SJF_P) return newThread.timeLeft < current.timeLeft;
        return false;  //default
    }
}




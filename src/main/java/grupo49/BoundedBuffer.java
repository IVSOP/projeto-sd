package grupo49;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// public class BoundedBuffer<T> {
// 	private T[] arr;
// 	private int size; // elements in array;

// 	private ReentrantLock lock;
// 	private Condition notEmpty;
// 	private Condition notFull;

// 	public BoundedBuffer(int _size) {
// 		arr = (T[]) new Object[size]; // ah??????????????????????????????????????????????????????
// 		this.size = 0;
// 		lock = new ReentrantLock();
// 		notEmpty = lock.newCondition();
// 		notFull = lock.newCondition();
// 	}

// 	public void push(T item) throws InterruptedException {
// 		try {
// 			lock.lock();
	
// 			while (size >= arr.length) { // full
// 				notFull.await();
// 			}
	
// 			arr[size] = item;
// 			size++;
	
// 			notEmpty.signal();
// 		} finally {
// 			lock.unlock();
// 		}
// 	}

// 	public T pop() throws InterruptedException {
// 		T item = null;
// 		try {
// 			lock.lock();

// 			while (size < 1) { // empty
// 				notEmpty.await();
// 			}

// 			size--;
// 			item = arr[size];

// 			notFull.signal();
// 		} finally {
// 			lock.unlock();
// 		}
// 		return item; // ???????
// 	}
// }

import org.apache.commons.collections4.queue.CircularFifoQueue;

// implementacao acima usa array estatico, acaba por nao causar muita starvation, nao e FIFO
// tbm nao quero usar Queue porque nao tem tamanho fixo
// decidi usar CircularFifoQueue
public class BoundedBuffer<T> {
	private CircularFifoQueue<T> queue;

	private ReentrantLock lock;
	private Condition notEmpty;
	private Condition notFull;

	public BoundedBuffer(int size) {
		this.queue = new CircularFifoQueue<T>(size);
		lock = new ReentrantLock();
		notEmpty = lock.newCondition();
		notFull = lock.newCondition();
	}

	// circular queue important methods
	// isAtFullCapacity()
	// isEmpty()
	// maxSize()
	// add()
	// remove()
	// size()

	public void push(T item) throws InterruptedException {
		try {
			lock.lock();
	
			while (queue.isFull()) { // full
				notFull.await();
			}
	
			queue.add(item);
	
			notEmpty.signal();
		} finally {
			lock.unlock();
		}
	}

	public T pop() throws InterruptedException {
		T item = null;
		try {
			lock.lock();

			while (queue.isEmpty()) { // empty
				notEmpty.await();
			}

			item = queue.remove();

			notFull.signal();
		} finally {
			lock.unlock();
		}
		return item; // ???????
	}
}
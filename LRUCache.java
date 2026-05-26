package pt;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bala-zpt0052 on 10/04/26
 *
 */
public class LRUCache
{
	static class Node
	{
		int key, value;
		Node prev, next;
		public Node(int key, int value)
		{
			this.key = key;
			this.value = value;
		}
	}

	private int capacity;
	private Map<Integer, Node> map;
	private Node head;
	private Node tail;

	public LRUCache(int capacity)
	{
		if (capacity <= 0) {
			throw new IllegalArgumentException("capacity must be > 0");
		}
		this.capacity = capacity;
		this.map = new HashMap<>();

		head = new Node(0, 0);
		tail = new Node(0, 0);

		head.next = tail;
		tail.prev = head;
	}

	public int get(int key)
	{
		Node node = map.get(key);
		if(node == null)
		{
			return -1;
		}
		moveToHead(node);
		return node.value;
	}

	public void put(int key, int value)
	{
		if(map.containsKey(key))
		{
			Node node = map.get(key);
			node.value = value;
			moveToHead(node);
			return;
		}

		Node newNode = new Node(key, value);
		map.put(key, newNode);
		// New node is not yet linked in the list; add directly to head
		addToHead(newNode);

		if(map.size() > capacity)
		{
			Node lr = removeTail();
			map.remove(lr.key);
		}
	}

	private void moveToHead(Node node)
	{
		// Only remove if the node is already linked in the list
		if (node.prev != null && node.next != null) {
			removeNode(node);
		}
		addToHead(node);
	}

	private void addToHead(Node node)
	{
		node.next = head.next;
		node.prev = head;
		head.next.prev = node;
		head.next = node;
	}

	private void removeNode(Node node)
	{
		// Make removal null-safe: node may not be linked
		if (node.prev != null) {
			node.prev.next = node.next;
		}
		if (node.next != null) {
			node.next.prev = node.prev;
		}
		node.prev = null;
		node.next = null;
	}

	private Node removeTail()
	{
		Node lr = tail.prev;
		if (lr == head) {
			return null;
		}
		removeNode(lr);
		return lr;
	}
}

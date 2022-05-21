package model;

import java.util.Objects;

public class HashResult {

	private int nonce;
	private String hash;
	private boolean complete = false;
	
	public HashResult() {}
	
	public int getNonce() {
		return nonce;
	}

	public String getHash() {
		return hash;
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public synchronized void foundAHash(String hash, int nonce) {
		this.hash = hash;
		this.nonce = nonce;
		this.complete = true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof HashResult)) return false;
		HashResult that = (HashResult) o;
		return getNonce() == that.getNonce() && isComplete() == that.isComplete() && Objects.equals(getHash(), that.getHash());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getNonce(), getHash(), isComplete());
	}
}

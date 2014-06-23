package awqatty.b.FunctionDictionary;

public enum FunctionType {
	SOURCE,		// denotes ListTree container (remove?)
	BLANK,
	NUMBER,
	ADD,
	SUBTRACT,
	MULTIPLY,
	DIVIDE,
	POWER,
	SQUARE,
	SQRT;
	
	// Property-Check Methods
	public boolean isFunction() {
		switch (this) {
		case SOURCE:
		case BLANK:
		case NUMBER:
			return false;
		default:
			return true;
		}
	}
	public boolean isCommutative() {
		switch (this) {
		case ADD:
		case MULTIPLY:
			return true;
		default:
			return false;	
		}
	}
	
	public boolean doesEncapsulateBranches() {
		switch (this) {
		case DIVIDE:
		case SQRT:
			return true;
		default:
			return false;
		}
	}
	
	public boolean isEncapsulated() {
		switch(this) {
		case BLANK:
		case NUMBER:
		case DIVIDE:
		case SQRT:
		case POWER:
			return true;
		default:
			return false;
		}
	}
}

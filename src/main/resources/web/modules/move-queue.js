// A request-move mutates the board too. Never cancel it to start a teleport or
// another move: wait for the entire request (including its state read) to settle.
export class MoveQueue {
	constructor() {
		this.tail = Promise.resolve();
	}

	run(request) {
		const result = this.tail.then(request);
		this.tail = result.catch(() => {});
		return result;
	}
}

/* Uncaught exception
 * Output: None. */ 

function throwsException() {
	throw new Error();
}

function doesntThrowException() {
	console.log("Hello world!");
}

function doSomething() {
 	try {
		throwsException();
		doesntThrowException();
	} catch (e) { }
}

doSomething();

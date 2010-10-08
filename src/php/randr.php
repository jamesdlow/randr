<?php
/* 
 * File: randr.php
 * Date: 13/08/2008
 * Website: http://code.google.com/p/randr/
 * About: RandR (REST and relaxtion) generic REST library
 * Version: ${new.version}
 */

class BaseException extends Exception {
	function __construct($errorMsg, $number = -1, $exception = null) {
		parent::__construct($errorMsg . ($exception == null ? '' : RestClass::DELIM . " " . $exception->getMessage()), $number);
		$this->exception = $exception;
	}
}

class RestException extends BaseException {
	function __construct($errorMsg, $number = -1, $exception = null) {
		parent::__construct($errorMsg, $number, $exception);
	}
}

class ResponseException extends BaseException {
	function __construct($errorMsg, $number = -1, $exception = null) {
		parent::__construct($errorMsg, $number, $exception);
	}
}

class RestResult {
	function __construct($result = null, $status = null, $code = null, $message = null) {
		$this->result = $result;
		if ($result != null) {
			$xml = new SimpleXMLElement($result);
			//print_r($xml);
			$status = RestClass::STATUS;
			$code = RestClass::CODE;
			$message = RestClass::MESSAGE;
			$this->status = strtoupper($xml->$status) == 'SUCCESS';
			$this->code = $xml->$code;
			$this->message = $xml->$message;
		} else {
			$this->status = $status;
			$this->code = $code;
			$this->message = $message;
		}
	}
	function getStatus() {
		return $this->status;
	}
	function getCode() {
		return $this->code;
	}
	function getMessage() {
		return $this->message;
	}
	function getRawResult() {
		return $this->result;
	}
}

//Master RestClass which implements common code such as variable prefixes and signature checking
abstract class RestClass {
	const TIME = 'time';
	const SIG = 'sig';
	const APIKEY = 'apikey';
	const TEST = 'test';
	const TEST_ECHO = 'echo';
	const RESULT = 'result';
	const STATUS = 'status';
	const CODE = 'code';
	const MESSAGE = 'message';
	const DELIM = ';';
	const SUCCESS = 'SUCCESS';
	const ERROR = 'ERROR';
	
	function __construct ($apikey = null, $prefix = null) {
		$this->apikey = $apikey;
		$this->prefix = (isset($prefix) ? $prefix . '_' : '');
		$this->sigvar = $this->prefix . RestClass::SIG;
		$this->timevar = $this->prefix . RestClass::TIME;
		$this->apivar = $this->prefix . RestClass::APIKEY;
		$this->testvar = $this->prefix . RestClass::TEST;
		$this->reserved[] = $this->sigvar;
		$this->reserved[] = $this->timevar;
		$this->reserved[] = $this->apivar;
		$this->reserved[] = $this->testvar;
	}
	function setApiKey($apikey) {
		$this->apikey = $apikey;
	}
	//Check the params are signed with the api key and can therefore be trusted
	function signature($params) {
		$string = '';
		//Include the API key in the signature, even though we don't transmit
		$params[$this->apivar] = $this->apikey;
		//Sort alphabetically by key name
		ksort($params);
		foreach ($params as $key => $value) {
			if ($key != $this->sigvar &&
				($this->prefix == '' || substr($key,0,strlen($this->prefix)) == $this->prefix)) {
				$string .= $key . $value;
			}
		}
		return md5($string);
	}
}

//Rest service, a server wanting to process a rest request should extend this class
class RestService extends RestClass {
	function __construct ($apikey = null, $prefix = null, $timeout = 15, $params = null, $auto = true) {
		parent::__construct($apikey, $prefix);
		//If params are passed to this class, use them, otherwise use $_REQUEST
		if (isset($params)) {
			$this->params = $params;
		} else {
			$this->params = $_REQUEST;
		}
		$this->timeout = $timeout;
		//If auto is false, then consumers of this class will have to manually call process()
		if ($auto) {
			$this->process();
		}
	}
	//Main function that processes request
	function process() {
		try {
			if ($this->apikey == null) {
				$this->setApiKey($this->getApiKeyImplementation());
			}
			$this->validate();
			$test = $this->params[$this->testvar];
			if ($test == RestClass::TEST_ECHO) {
				$result = testImplementation();
			} else {
				$result = processImplementation();
			}
			success($result);
		} catch (RestException $e) {
			error($e->getMessage(), $e->getCode());
		}
	}
	//Get the api key from params or ip address or something, child classes should implement this function if the api key isn't specifiedy during construction
	function getApiKeyImplementation() {
		return '';
	}

	//Process the request, child classes should implement this function to process the request params
	function processImplementation() {}
	
	function testImplementation() {
		$firsttime = true;
		foreach ($params as $key => $value) {
			if ($firsttime) {
				$firsttime = false;
			} else {
				$message = $message . RestClass::DELIM;
			}
			$message = $message . $key . ":" . $value;
		}
		return $message;
	}
	
	function response($message, $code = 0, $success = true) {
		return "<".RestClass::RESULT."><".RestClass::STATUS.">".($success ? RestClass::SUCCESS : RestClass::ERROR)."</".RestClass::STATUS."><".RestClass::CODE.">".$code."</".RestClass::CODE."><".RestClass::MESSAGE."><![CDATA[".$message."]]></".RestClass::MESSAGE."></".RestClass::RESULT.">";
	}
	//Send success XML
	function success($msg, $code = 0) {
		echo $this->response($msg,$code,true);
	}
	//Send error XML
	function error($msg, $code = -1) {
		echo $this->response($msg,$code,false);
	}
	//Validate the common params
	function validate() {
		$time = $this->params[$this->timevar];
		if (!isset($time)) {
			throw new RestException('Time Variable Not Set.',1);
		}
		//Time should be in seconds
		if (time() - $time > $this->timeout) {
			throw new RestException('Time Difference Too Great.',2);
		}
		$sig = $this->params[$this->sigvar];
		if (!isset($sig)) {
			throw new RestException('Signature Variable Not Set.',3);
		}
		if ($sig != $this->signature($this->params)) {
			throw new RestException('Signature Invalid.',0);
		}
		return true;
	}
	//Get a parameter auto adding the prefix
	function getParam($param) {
		return $this->params[$this->prefix . $param];
	}
}

//Rest client, a client wanting to use a rest service should use this class
class RestClient extends RestClass {
	function __construct ($url, $apikey, $prefix = null) {
		parent::__construct($apikey, $prefix);
		$this->params = array();
		$this->url = $url;
	}
	//Add a parameter checking if its not one of the predefined reserved params
	function addParam($param, $value) {
		$full = $this->prefix . $param;
		foreach ($this->reserved as $check) {
			$this->checkParam($full, $check);
		}
		$this->params[$full] = $value;
	}
	//Check if a param is not equal
	function checkParam($param, $check) {
		if ($param == $check) {
			throw new RestException('Param name cannot equal ' . substr($param,strlen($this->prefix)),E_USER_ERROR);
		}
	}
	//Clear all params
	function clearParams() {
		$this->params = array();
	}
	//Get the params valid at this time
	function getParams($test = null) {
		$params = $this->params;
		if ($test != null && $test != '') {
			$params[$this->testvar] = $test;
		}
		$params[$this->timevar] = time();
		$params[$this->sigvar] = $this->signature($params);
		return $params;
	}

	function echoParams() {
		$this->sendParams(RestClass::TEST_ECHO);
	}

	function echoResult() {
		return new RestResult($this->sendParams(RestClass::TEST_ECHO));
	}

	function sendParams($test = null) {
		$r = new HTTPRequest($this->url);
		return $r->Get($this->getParams($test));
	}

	function getResult($test = null) {
		return new RestResult($this->sendParams($test));
	}
}

class HTTPRequest {
	var $_fp;			// HTTP socket
	var $_url;			// full URL
	var $_host;			// HTTP host
	var $_protocol;		// protocol (HTTP/HTTPS)
	var $_uri;			// request URI
	var $_port;			// port
	var $_params;		//Additional get params not on the url

	// scan url
	function _scan_url() {
		$req = $this->_url;
		$pos = strpos($req, '://');
		$this->_protocol = strtolower(substr($req, 0, $pos));
		$req = substr($req, $pos+3);
		$pos = strpos($req, '/');
		if($pos === false) {
			$pos = strlen($req);
		}
		$host = substr($req, 0, $pos);

		if(strpos($host, ':') !== false) {
			list($this->_host, $this->_port) = explode(':', $host);
		} else {
			$this->_host = $host;
			$this->_port = ($this->_protocol == 'https') ? 443 : 80;
		}

		$this->_uri = substr($req, $pos);
		if($this->_uri == '') {
			$this->_uri = '/';
		}
	}

	// constructor
	function __construct($url) {
		$this->_url = $url;
		$this->_scan_url();
	}

	//Basic header common to both GET and POST
	function BasicHeader($type) {
		$crlf = "\r\n";
		//If has get params, add them
		return $type . ' ' . $this->_uri . ($this->_params == '' ? '' : '?'.$this->_params) . ' HTTP/1.0' . $crlf
			. 'Host: ' . $this->_host . $crlf;
	}

	//Encode request param
	function ParamEncode($key, $value) {
		return urlencode($key) . '=' . urlencode($value);
	}
	
	//Encode request params
	function RequestEncode($paramarray) {
		$content = '';
		//Encode all post parameters
		foreach($paramarray as $key => $value) {
			if ($content == '') {
				$content = $this->ParamEncode($key,$value);
			} else {
				$content .= '&' .$this->ParamEncode($key,$value);
			}
		}
		return $content;
	}
	
	//Get request
	function Get($paramarray = null, $details = false) {
		$crlf = "\r\n";
		if (isset($paramarray)) {
			$this->_params = $this->RequestEncode($paramarray);
		} else {
			$this->_params = '';
		}
		$req = $this->BasicHeader('GET') . $crlf;
		return $this->Request($req,false,$details);
	}

	//Post request
	function Post($paramarray, $details = false) {
		$crlf = "\r\n";
		$this->_params = '';
		$content = $this->RequestEncode($paramarray);
		$req = $this->BasicHeader('POST')
			. 'Content-Type: application/x-www-form-urlencoded' . $crlf
			. 'Content-Length: ' . strlen($content) . $crlf
			. $crlf
			. $content;
		return $this->Request($req,true,$details);
	}

	//General request
	function Request($req, $post = false, $details = false) {
		$crlf = "\r\n";
		
		// fetch
		$this->_fp = fsockopen(($this->_protocol == 'https' ? 'ssl://' : '') . $this->_host, $this->_port);
		fwrite($this->_fp, $req);
		$response = '';
		while(is_resource($this->_fp) && $this->_fp && !feof($this->_fp)) {
			$response .= fread($this->_fp, 1024);
		}
		fclose($this->_fp);

		// split header and body
		$pos = strpos($response, $crlf . $crlf);
		if($pos === false)
			return($response);
		$header = substr($response, 0, $pos);
		$body = substr($response, $pos + 2 * strlen($crlf));

		// parse headers
		$headers = array();
		$lines = explode($crlf, $header);
		$firsttime = true;
		foreach($lines as $line) {
			if ($firsttime) {
				$codes = explode(" ", $line);
				$code['version'] = $codes[0];
				$code['code'] = intval($codes[1]);
				$code['message'] = $codes[2];
				$firsttime = false;
			}
			if(($pos = strpos($line, ':')) !== false) {
				$headers[strtolower(trim(substr($line, 0, $pos)))] = trim(substr($line, $pos+1));
			}
		}

		// redirection?
		if(isset($headers['location'])) {
			$http = new HTTPRequest($headers['location']);
			return $http->Request($req, $post, $details);
		} else {
			if ($details) {
				$result['http'] = $code;
				$result['header'] = $headers;
				$result['body'] = $body;
				return $result;
			} else {
				return $body;
			}
		}
	}

	//Download URL to string, included for backwards compatibilty with versions that did have seperate GET/POST
	function DownloadToString() {
		return $this->Get(null,false);
	}
}
?>
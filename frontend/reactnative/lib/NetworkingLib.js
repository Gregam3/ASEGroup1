import request from 'superagent';

var ip = 'http://ec2-18-130-151-76.eu-west-2.compute.amazonaws.com:8080/development/';

export function postJSON(extension, jsonFile) {
    console.log("Attempting to post to " + extension);

    //TODO change ip to reference your local/aws server
    request.post(ip + extension)
        .set('Content-Type', 'application/json')
        .send(jsonFile)
        .end(function (err, res) {
            console.log("Response: " + JSON.stringify((err) ? err : res.statusCode));
        });
}

export async function get(extention, data){
	return await request.get(ip + extention + data).then(res => {
			if(res.body != null){
				return JSON.stringify(res.body);
			}else{
				return res;
			}
		}).catch(err => {
			return err
		});
}

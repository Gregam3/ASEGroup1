import request from 'superagent';

export function postJSON(extention, jsonFile){

    //TODO change ip to reference your local/aws server
    request.post('http://192.168.0.2:8080/'+extention)
    .set('Content-Type', 'application/json')
    .send(jsonFile)
    .end(function(err, res){
        console.log("Response " + JSON.stringify(res));
        console.log("Error " +JSON.stringify(err));
    });
}
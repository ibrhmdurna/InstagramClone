const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

exports.followRequest = functions.database.ref("/Follow_req/{reqUID}/{currentUID}").onCreate((data, context) => {

    const req_id = context.params.reqUID;
    const current_id = context.params.currentUID;

    console.log("Current UID : ", current_id);
    console.log("Request UID : ", req_id);

    const fcm_token = admin.database().ref(`/Users/${current_id}/fcm_token`).once('value');
    const user_name = admin.database().ref(`/Users/${req_id}/user_name`).once('value');
    const full_name = admin.database().ref(`/Users/${req_id}/full_name`).once('value');
    const user_profile_url = admin.database().ref(`/Users/${req_id}/details/profile_image`).once('value');

    return Promise.all([fcm_token, user_name, full_name, user_profile_url]).then(result => {

        const current_token = result[0].val();
        const req_user_name = result[1].val();
        const req_full_name = result[2].val();
        const req_profile_image = result[3].val();

        console.log("Current Token : ", current_token);
        console.log("Req User Name : ", req_user_name);

        const payload ={
            data: {
                type : "Follow_Request",
                user_name : `${req_user_name}`,
                full_name : `${req_full_name}`,
                user_image : `${req_profile_image}`,
                user_id : `${req_id}`,
                icon : "default",
                click_action : ".NewsActivity"
            }
        };

        return admin.messaging().sendToDevice(current_token, payload).then(Response =>{
            console.log("Notification sent :)");
        });
    });
});

exports.followAccept = functions.database.ref("/News/Follow_interactions/{acceptUID}/{currentUID}").onCreate((data, context) => {

    const accept_id = context.params.acceptUID;
    const current_id = context.params.currentUID;

    console.log("Current UID : ", current_id);
    console.log("Accept UID : ", accept_id);

    const fcm_token = admin.database().ref(`/Users/${current_id}/fcm_token`).once('value');
    const user_name = admin.database().ref(`/Users/${accept_id}/user_name`).once('value');
    const full_name = admin.database().ref(`/Users/${accept_id}/full_name`).once('value');
    const user_profile_url = admin.database().ref(`/Users/${accept_id}/details/profile_image`).once('value');

    return Promise.all([fcm_token, user_name, full_name, user_profile_url]).then(result => {

        const current_token = result[0].val();
        const accept_user_name = result[1].val();
        const accept_full_name = result[2].val();
        const accept_profile_image = result[3].val();

        console.log("Current Token : ", current_token);
        console.log("Req User Name : ", accept_user_name);

        const payload ={
            data: {
                type : "Follow_Accept",
                user_name : `${accept_user_name}`,
                full_name : `${accept_full_name}`,
                user_image : `${accept_profile_image}`,
                user_id : `${accept_id}`,
                icon : "default",
                click_action : ".UserProfileActivity"
            }
        };

        return admin.messaging().sendToDevice(current_token, payload).then(Response =>{
            console.log("Notification sent :)");
        });
    });
});

exports.sendMessage = functions.database.ref("/Messages/{receiveMessageUID}/{sendMessageUID}/{messageID}").onCreate((data, context) => {

    const receiveUID = context.params.receiveMessageUID;
    const sendUID = context.params.sendMessageUID;
    const messageId = context.params.messageID;

    console.log("Receive UID : ", receiveUID);
    console.log("Send UID : ", sendUID);
    console.log("Message ID : ", messageId);

    const token = admin.database().ref(`/Users/${receiveUID}/fcm_token`).once('value');
    const user_name = admin.database().ref(`/Users/${sendUID}/user_name`).once('value');
    const full_name = admin.database().ref(`/Users/${sendUID}/full_name`).once('value');
    const user_image = admin.database().ref(`/Users/${sendUID}/details/profile_image`).once('value');
    const last_message = admin.database().ref(`/Messages/${receiveUID}/${sendUID}/${messageId}`).once('value');

    return Promise.all([token, user_name, full_name, user_image, last_message]).then(result => {

        const receive_token = result[0].val();
        const send_user_name = result[1].val();
        const send_full_name = result[2].val();
        const send_user_image = result[3].val();
        const send_last_message = result[4].child('message').val();
        const send_user_id = result[4].child('from').val();

        if(send_user_id == sendUID){

            const payload ={
                data: {
                    type : "Message",
                    user_name : `${send_user_name}`,
                    full_name : `${send_full_name}`,
                    user_image : `${send_user_image}`,
                    user_id : `${send_user_id}`,
                    message : `${send_last_message}`,
                    icon : "default",
                    click_action : ".ChatActivity"
                }
            };
    
            return admin.messaging().sendToDevice(receive_token, payload).then(Response =>{
                console.log("Message notification sent :)");
            });

        }
    });
});

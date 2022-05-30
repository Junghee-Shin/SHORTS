<?php
$target="uploads/".basename($_FILES['image']['image']);

echo {
    "info" : "uploaded",
    "status" : 200
}

// if(isset($_REQUEST['submit'])){
// 	if(move_uploaded_file($_FILES['files']['tmp_name'],$target)){
// 		echo "uploaded";
// 	}else {
// 		echo "failed";
// 	}
// }
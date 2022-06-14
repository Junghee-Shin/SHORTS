<?php
    $file_path = "uploads/";

    $file_path = $file_path . basename( $_FILES['image']['name']);
    if(move_uploaded_file($_FILES['image']['tmp_name'], $file_path)) {
		echo json_encode(array(
			"info" => "uploaded",
			"status" => 200,
		));
		
    } else{
		echo json_encode(array(
			"info" => "fail",
			"status" => 500,
		));
    }
 ?>
<?php
	$image = $_POST['image'];
	
    //$data = $_POST['image'];
	//$json = json_decode($data); 
	//$image = $json->image;

	if(unlink("./uploads/".$image)) {
		echo json_encode(array(
			"info" => "deleted",
			"status" => 200,
		));
		
    } else{
		echo json_encode(array(
			"info" => "fail",
			"status" => 500,
		));
    }

 ?>
select `c`.`name` AS `category`, sum(`p`.`amount`) AS `total_sales`
from (((((`sakila`.`payment` `p`
    join `sakila`.`rental` `r` on ((`p`.`rental_id` = `r`.`rental_id`)))
    join `sakila`.`inventory` `i` on ((`r`.`inventory_id` = `i`.`inventory_id`)))
    join `sakila`.`film` `f` on ((`i`.`film_id` = `f`.`film_id`)))
    join `sakila`.`film_category` `fc` on ((`f`.`film_id` = `fc`.`film_id`)))
         join `sakila`.`category` `c` on ((`fc`.`category_id` = `c`.`category_id`)))
group by `c`.`name`
order by `total_sales` desc
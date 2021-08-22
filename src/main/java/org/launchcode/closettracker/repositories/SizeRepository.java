package org.launchcode.closettracker.repositories;

import org.launchcode.closettracker.models.Size;
import org.springframework.data.repository.CrudRepository;

public interface SizeRepository extends CrudRepository<Size, Integer>  {
}
